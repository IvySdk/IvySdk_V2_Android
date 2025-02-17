package com.ivy.sdk.base.ads


import android.app.Activity
import android.os.SystemClock
import android.view.View
import android.widget.FrameLayout
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

abstract class IAdLoader(val adProperty: AdProperty, val adConfig: AdConfig, val pamManager: IPAMManager?, val adListener: IAdListener) :
    IInternalAdListener {

    companion object {
        const val TAG = "AdLoader"
    }

    //请求失败次数
    private var requestFailureCount: Int = 0

    //公共事件属性
    protected val eventParams: MutableMap<String, Any> = mutableMapOf()

    private var tag: String = "default"
    private var placementId: Int = 0
    private var clientInfos: MutableMap<String, Any> = mutableMapOf()

    protected var isBannerReady = false
    private var isAdLoading = false

    private var timeStartLoad: Long = 0
    private var timeStartShow: Long = 0

    //请求超时器
    private var adLoadTimeMonitor: AdLoadTimer = object : AdLoadTimer(adConfig.adLoadTimeOut * 1000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            ILog.i(TAG, "ad load timeout::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
            onAdLoadFailure("ad load timeout")
        }

    }

    init {
        resetEventParams()
    }

    open fun loadAd() {
        if (!Util.isNetworkConnected()) {
            onReloadAd(true)
            return
        }
        if (adProperty.adType == AdType.BANNER && isBannerReady) return
        if (isAdLoading) return
        if (adProperty.adUnit.isEmpty()) {
            onAdLoadFailure("invalid ad unit id")
            return
        }
        adLoadTimeMonitor.cancel()
        adLoadTimeMonitor.start()
        isAdLoading = true
        resetEventParams()
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "03")
        params.putAll(eventParams)
        logEvent(EventIDs.AD_REQUEST, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        timeStartLoad = SystemClock.elapsedRealtime()
        doLoadAd()
    }

    protected abstract fun doLoadAd()

    abstract fun isReady(): Boolean

    open fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        setupPlacementInfo(tag, placement, clientInfo)
    }

    open fun getBannerAdView(): View? = null

    open fun showBannerAd(container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean {
        setupPlacementInfo(tag, placement, clientInfo)
        return false
    }

    open fun closeBannerAd(placement: Int) {}

    open fun getAdUnit(): String = adProperty.adUnit

    open fun onResume(container: FrameLayout?) {}

    open fun onPause(container: FrameLayout?) {}

    open fun onDestroy() {}

    private fun onReloadAd(isFailed: Boolean) {
        if (isFailed) {
            requestFailureCount++
            //广告请求失败，为避免高频请求导致无填充，逐步延迟加载
            CoroutineScope(Dispatchers.Default).launch {
                if (requestFailureCount > adConfig.timesDelayOnLoadFail) {
                    requestFailureCount = 0
                }
                val sleepTime = requestFailureCount * adConfig.delayOnLoadFail * 1000L
                ILog.i(TAG, "sleep $sleepTime for::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
                delay(sleepTime)
                launch(Dispatchers.Main) {
                    loadAd()
                }
            }
        } else {
            //广告关闭或其它处理，直接加载
            loadAd()
        }
    }

    override fun onAdLoadSuccess() {
        val loadDuration = SystemClock.elapsedRealtime() - timeStartLoad
        if (adProperty.adType == AdType.BANNER) {
            isBannerReady = true
        }
        isAdLoading = false
        adLoadTimeMonitor.cancel()
        ILog.i(TAG, "ad load succeed::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        adListener.onAdLoadSuccess(adProperty.adType)
        requestFailureCount = 0
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "04")
        params.putAll(eventParams)
        params[EventParams.EVENT_PARAM_AD_RESPONSE_LATENCY] = loadDuration
        logEvent(EventIDs.AD_LOAD_SUCCESS, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun onAdLoadFailure(reason: String?) {
        val loadDuration = SystemClock.elapsedRealtime() - timeStartLoad
        if (adProperty.adType == AdType.BANNER) {
            isBannerReady = false
        }
        isAdLoading = false
        adLoadTimeMonitor.cancel()
        ILog.i(TAG, "ad load failed::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        adListener.onAdLoadFailure(adProperty.adType, reason)
        onReloadAd(true)
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "05")
        params.putAll(eventParams)
        reason?.let { params[EventParams.EVENT_PARAM_FAIL_REASON] = it }
        params[EventParams.EVENT_PARAM_AD_RESPONSE_LATENCY] = loadDuration
        logEvent(EventIDs.AD_LOAD_FAILED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun onAdShowSuccess() {
        if (adProperty.adType == AdType.BANNER) {
            isBannerReady = false
        } else {
            timeStartShow = SystemClock.elapsedRealtime()
        }
        ILog.i(TAG, "ad show succeed::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        adListener.onAdShowSuccess(adProperty.adType, tag, placementId)
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "09")
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        logEvent(EventIDs.AD_SHOW_SUCCESS, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun onAdShowFailed(reason: String?) {
        if (adProperty.adType == AdType.BANNER) {
            isBannerReady = false
        }
        ILog.i(TAG, "ad show failed::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        adListener.onAdShowFailed(adProperty.adType, reason, tag, placementId)
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "10")
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        reason?.let { params[EventParams.EVENT_PARAM_FAIL_REASON] = it }
        logEvent(EventIDs.AD_SHOW_FAILED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun onAdClicked() {
        ILog.i(TAG, "ad clicked::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        adListener.onAdClicked(adProperty.adType, tag, placementId)
        val params = mutableMapOf<String,Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "11")
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        logEvent(EventIDs.AD_CLICKED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun onAdClosed(gotReward: Boolean) {
        ILog.i(TAG, "ad closed::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "12")
        if (adProperty.adType == AdType.BANNER) {
            val showDuration = SystemClock.elapsedRealtime() - timeStartShow
            params[EventParams.EVENT_PARAM_AD_SHOW_DURATION] = showDuration
        }
        adListener.onAdClosed(adProperty.adType, gotReward, tag, placementId)
        onReloadAd(false)
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        logEvent(EventIDs.AD_CLOSED, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        resetPlacementInfo()
    }

    override fun onUserRewarded() {
        ILog.i(TAG, "user rewarded::${adProperty.platform.value}:${adProperty.adType.value}:${adProperty.adUnit}")
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "13")
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        logEvent(EventIDs.AD_REWARD_USER, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }

    open fun onAdRevenuePaid(revenue: Double, currency: String) {
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_REVENUE to revenue, EventParams.EVENT_PARAM_CURRENCY to currency)
        params.putAll(eventParams)
        params.putAll(clientInfos)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placementId
        adListener.logEvent("tmp_event", EventType.EVENT_TYPE_AD_REVENUE, EventSrc.EVENT_SRC_SDK, params)
    }

    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {
        super.logEvent(eventName, eventType, eventSrc, params, platforms)
        adListener.logEvent(eventName, eventType, eventSrc, params, platforms)
    }

    private fun resetEventParams() {
        eventParams.clear()
        eventParams[EventParams.EVENT_PARAM_AD_UNIT] = adProperty.adUnit
        eventParams[EventParams.EVENT_PARAM_AD_FORMAT] = adProperty.adType.value
        eventParams[EventParams.EVENT_PARAM_AD_NETWORK] = adProperty.platform.value
      //  eventParams[EventParams.EVENT_PARAM_AD_TYPE] = adProperty.adType.value
        eventParams[EventParams.EVENT_PARAM_COUNTRY] = Locale.getDefault().country
    }

    private fun setupPlacementInfo(tag: String, placement: Int, clientInfo: String?) {
        this.tag = tag
        this.placementId = placement
        if (!clientInfo.isNullOrEmpty()) {
            try {
                val json = JSONObject(clientInfo)
                for (key in json.keys()) {
                    val value = json.get(key)
                    if (value is Boolean) {
                        clientInfos[key] = if (value) 1 else 0
                    } else {
                        clientInfos[key] = value
                    }
                }
            } catch (e: Exception) {
                ILog.e(TAG, "format client info failed:${e.message}")
            }
        } else {
            clientInfos.clear()
        }
    }

    private fun resetPlacementInfo() {
        tag = "default"
        placementId = 0
        clientInfos.clear()
    }


}