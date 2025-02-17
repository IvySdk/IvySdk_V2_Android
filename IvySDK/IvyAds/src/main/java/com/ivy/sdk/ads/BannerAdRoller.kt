package com.ivy.sdk.ads

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.BannerPosition
import com.ivy.sdk.base.ads.CycleTimer
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class BannerAdRollerTask(
    val position: Int,
    val tag: String,
    val placement: Int,
    val clientInfo: String?,
    val adConfig: AdConfig,
    val debug: Boolean
) {

    var currentShowingAdUnitId: String? = null
    private var lastShowTime: Long = 0
    var container: FrameLayout? = null

    companion object {
        const val TAG = "BannerAdRollerTask"
        const val KEY_FLAG_BANNER_CONTAINER = "flag_banner"
    }

    fun prepare() {
        ILog.i(TAG, "banner task:$tag;$position;$placement add success")
        CoroutineScope(Dispatchers.Main).launch {
            container = createBannerContainer()
            if (container == null) {
                ILog.e(TAG, "init banner container failed:$tag")
            }
        }
    }

    fun showBannerAd(ivyBaseAd: IvyBaseAd, adUnit: String): Boolean {
        if (container == null) {
            CoroutineScope(Dispatchers.Main).launch {
                container = createBannerContainer()
            }
        }
        if (container != null) {
            container?.removeAllViews()
//            (adView.parent as? ViewGroup)?.removeView(adView)
//            container?.addView(adView)
            val state = ivyBaseAd.showBannerAd(adUnit, container!!, tag, placement, clientInfo)
            if (state) {
                lastShowTime = System.currentTimeMillis()
                return true
            }
        }
        return false
    }

    fun onResume() {

    }

    fun onPause() {

    }

    fun showBannerAd(adView: View?): Boolean {
        if (adView == null) return false
        if (container == null) {
            CoroutineScope(Dispatchers.Main).launch {
                container = createBannerContainer()
            }
        }
        if (container != null) {
            container?.removeAllViews()
            (adView.parent as? ViewGroup)?.removeView(adView)
            container?.addView(adView)
            lastShowTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun closeBannerAd() {
        try {
            currentShowingAdUnitId = null
            container?.removeAllViews()
            (container?.parent as? ViewGroup)?.removeView(container)
        } catch (_: Exception) {

        }
    }

    fun readyRefresh(): Boolean {
        if (adConfig.bannerRefreshByPlatform) {
            if (container != null && container!!.childCount > 0) {
                return false
            }
        } else {
            val duration = System.currentTimeMillis() - lastShowTime
            return duration > adConfig.bannerAdRefreshDuration * 1000
        }
        return true
    }

    private fun createBannerContainer(): FrameLayout? {
        try {
            val rootView: FrameLayout = ActivityUtil.Instance.activity?.window?.decorView as? FrameLayout ?: return null
            val layoutTag = "${KEY_FLAG_BANNER_CONTAINER}_${tag}_$placement"
            var container1: FrameLayout? = rootView.findViewWithTag<FrameLayout>(layoutTag)
            if (container1 != null) {
                rootView?.removeView(container1)
            }
            // rootView.removeView(container1)
            container1 = FrameLayout(App.Instance)
            val gravity = when (position) {
                BannerPosition.POSITION_LEFT_TOP -> Gravity.TOP or Gravity.LEFT
                BannerPosition.POSITION_CENTER_TOP -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
                BannerPosition.POSITION_RIGHT_TOP -> Gravity.TOP or Gravity.RIGHT
                BannerPosition.POSITION_LEFT_BOTTOM -> Gravity.BOTTOM or Gravity.BOTTOM
                BannerPosition.POSITION_CENTER_BOTTOM -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                BannerPosition.POSITION_RIGHT_BOTTOM -> Gravity.RIGHT or Gravity.BOTTOM
                BannerPosition.POSITION_CENTER -> Gravity.CENTER
                else -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            }
            val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, Util.dp2Px(App.Instance, 60.0f).toInt(), gravity)
            if (debug) {
                container1.setBackgroundColor(Color.GREEN)
            }
            container1.tag = layoutTag
            rootView.addView(container1, layoutParams)
            return container1
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}

internal interface IBannerAdRollerHelper {
    fun getIvyBannerAd(): IvyBaseAd?
}

internal class BannerAdRoller {

    companion object {
        const val TAG = "BannerAdRoller"
        val lock = Any()
    }

    private var isRolling: Boolean = false
    private var rollerSleepDuration: Long = 1000
    private val rollerTasks: MutableMap<String, BannerAdRollerTask> = mutableMapOf()

    private var helper: IBannerAdRollerHelper? = null

    private val cycleTimer: CycleTimer = object : CycleTimer(60 * 1000, rollerSleepDuration) {
        override fun onTick(millisUntilFinished: Long) {
            refreshAd()
        }

        override fun onSection() {

        }

    }

    constructor(helper: IBannerAdRollerHelper?) {
        this.helper = helper

    }

    fun addRollerTask(task: BannerAdRollerTask) {
        val tag = "${task.placement}"
        removeRollerTask(tag)
        rollerTasks[tag] = task
        task.prepare()
    }

    fun removeRollerTask(tag: String): String? {
        var currentShowAdUnit: String? = null
        if (rollerTasks[tag] != null) {
            //关闭任务
            rollerTasks[tag]?.closeBannerAd()
            rollerTasks[tag]?.currentShowingAdUnitId?.let {
                currentShowAdUnit = it
                helper?.getIvyBannerAd()?.reloadBannerAd(it)
            }
            rollerTasks.remove(tag)
        }
        return currentShowAdUnit
    }

    fun startRoller() {
        isRolling = true
        cycleTimer.reset()
    }

    fun stopRoller() {
        isRolling = false
        cycleTimer.cancel()
    }

    fun onResume() {
        try {
            rollerTasks.values.forEach {
                helper?.getIvyBannerAd()?.onResume(it.container)
            }
        } catch (_: Exception) {
        }
    }

    fun onPause() {
        try {
        rollerTasks.values.forEach {
            helper?.getIvyBannerAd()?.onPause(it.container)
        }
        } catch (_: Exception) {
        }
    }

    fun onDestroy() {
        try {
            helper?.getIvyBannerAd()?.onDestroy()
        } catch (_: Exception) {
        }
    }

    fun isRolling(): Boolean = isRolling

    fun onBannerAdLoaded() {
        if (rollerTasks.isNotEmpty() && !isRolling) {
            startRoller()
        }
    }

    private fun refreshAd() {
        synchronized(BannerAdRoller.lock) {
            if (!isRolling) return
            helper?.getIvyBannerAd()?.let { ivyBaseAd ->
                if (rollerTasks.isEmpty()) return@let
                val adUnits: MutableList<String> = ivyBaseAd.getAdUnits()
                if (adUnits.isEmpty()) {
                    ILog.i(TAG, "no banner ad unit found")
                    return@let
                }
                for (adUnit in adUnits) {
                    val isReady = ivyBaseAd.isBannerAdReady(adUnit)
                    if (isReady) {
                        for (task in rollerTasks.values) {
                            if (task.readyRefresh()) {
                                ILog.i(TAG, "banner call refresh")
                                //  val adView = ivyBaseAd.getBannerAdView(adUnit)
                                val result = task.showBannerAd(ivyBaseAd, adUnit)
                                if (result) {
                                    ILog.i(TAG, "task:${task.tag};${task.position} refresh success")
                                    if (task.currentShowingAdUnitId != null) {
                                        ivyBaseAd.reloadBannerAd(task.currentShowingAdUnitId!!)
                                    }
                                    task.currentShowingAdUnitId = adUnit
                                }
                            }
                        }
                    }
                }
            } ?: ILog.i(TAG, "refresh banner ad failed;invalid ad provider")
        }
    }


}