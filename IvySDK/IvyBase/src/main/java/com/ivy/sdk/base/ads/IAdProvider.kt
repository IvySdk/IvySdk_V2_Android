package com.ivy.sdk.base.ads

import android.view.View
import android.widget.FrameLayout
import com.ivy.sdk.base.utils.ActivityUtil

/**
 * 广告格式
 */
enum class AdType(val value: String) {
    BANNER("banner"),
    INTERSTITIAL("interstitial"),
    REWARDED("rewarded"),
    NATIVE("native"),
    SPLASH("splash"),
}

/**
 * banner位置
 */
interface BannerPosition {

    companion object {
        const val POSITION_LEFT_TOP = 1
        const val POSITION_CENTER_TOP = 3
        const val POSITION_RIGHT_TOP = 6
        const val POSITION_LEFT_BOTTOM = 2
        const val POSITION_CENTER_BOTTOM = 4
        const val POSITION_RIGHT_BOTTOM = 7
        const val POSITION_CENTER = 5
    }

}

interface IAd {
    fun isInterstitialAdReady(adUnit: String): Boolean

    fun showInterstitialAd(adUnit: String, tag: String, placement: Int, clientInfo: String?)

    fun isRewardedAdReady(adUnit: String): Boolean

    fun showRewardedAd(adUnit: String, tag: String, placement: Int, clientInfo: String?)

    fun isBannerAdReady(adUnit: String): Boolean

    fun getBannerAdView(adUnit: String): View?

    fun showBannerAd(adUnit: String, container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean

    fun closeBannerAd(adUnit: String, placement: Int)

    fun loadBannerAd(adUnit: String)
}

abstract class IAdProvider : IAd {

    protected var debug: Boolean = false
    protected lateinit var adConfig: AdConfig
    protected lateinit var adListener: IAdListener

    protected open var pamManager:IPAMManager? = null

    protected val bannerAdLoaderTasks: MutableMap<String, IAdLoader> = mutableMapOf()
    protected val interstitialAdLoaderTasks: MutableMap<String, IAdLoader> = mutableMapOf()
    protected val rewardedAdLoaderTasks: MutableMap<String, IAdLoader> = mutableMapOf()

    open fun setup(debug: Boolean, adConfig: AdConfig, adListener: IAdListener) {
        this.debug = debug
        this.adConfig = adConfig
        this.adListener = adListener
    }

    abstract fun addTask(adProperty: AdProperty)

    fun getPAMManager(): IPAMManager? = pamManager

    override fun isInterstitialAdReady(adUnit: String): Boolean = interstitialAdLoaderTasks[adUnit]?.isReady() ?: false

    override fun showInterstitialAd(adUnit: String, tag: String, placement: Int, clientInfo: String?) {
        ActivityUtil.Instance.activity?.let { activity ->
            interstitialAdLoaderTasks[adUnit]?.show(activity, tag, placement, clientInfo) ?: adListener.onAdShowFailed(
                AdType.INTERSTITIAL,
                "invalid ad task::$adUnit",
                tag,
                placement
            )
        } ?: adListener.onAdShowFailed(AdType.INTERSTITIAL, "invalid activity", tag, placement)
    }

    override fun isRewardedAdReady(adUnit: String): Boolean = rewardedAdLoaderTasks[adUnit]?.isReady() ?: false

    override fun showRewardedAd(adUnit: String, tag: String, placement: Int, clientInfo: String?) {
        ActivityUtil.Instance.activity?.let { activity ->
            rewardedAdLoaderTasks[adUnit]?.show(activity, tag, placement, clientInfo) ?: adListener.onAdShowFailed(
                AdType.REWARDED,
                "invalid ad task::$adUnit",
                tag,
                placement
            )
        } ?: adListener.onAdShowFailed(AdType.REWARDED, "invalid activity", tag, placement)
    }

    override fun isBannerAdReady(adUnit: String): Boolean = bannerAdLoaderTasks[adUnit]?.isReady() ?: false

    override fun getBannerAdView(adUnit: String): View? = bannerAdLoaderTasks[adUnit]?.getBannerAdView()

    override fun showBannerAd(adUnit: String, container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean {
        return bannerAdLoaderTasks[adUnit]?.showBannerAd(container, tag, placement, clientInfo) ?: false
    }

    override fun closeBannerAd(adUnit: String, placement: Int) {
        bannerAdLoaderTasks[adUnit]?.closeBannerAd(placement)
    }

    override fun loadBannerAd(adUnit: String) {
        bannerAdLoaderTasks[adUnit]?.loadAd()
    }

    open fun onResume(container:FrameLayout?){
        try {
            bannerAdLoaderTasks.values.forEach {
                it.onResume(container)
            }
        } catch (_: Exception) {
        }
    }

    open fun onPause(container:FrameLayout?){
        try {
            bannerAdLoaderTasks.values.forEach {
                it.onPause(container)
            }
        } catch (_: Exception) {
        }
    }

    open fun onDestroy() {
        try {
            bannerAdLoaderTasks.values.forEach {
                it.onDestroy()
            }
        } catch (_: Exception) {
        }
    }


}