package com.ivy.sdk.yandex

import android.app.Activity
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.utils.ILog
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import org.json.JSONObject

internal class RewardedAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) : IAdLoader(
    adProperty, adConfig, pamManager, adListener
) {

    private var rewardedAd: RewardedAd? = null
    private var rewardedAdLoader: RewardedAdLoader? = null
    private var gotReward = false


    override fun isReady(): Boolean = rewardedAd != null

    override fun doLoadAd() {
        rewardedAdLoader = RewardedAdLoader(App.Instance).apply {
            setAdLoadListener(object : RewardedAdLoadListener {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    val data = ad.info.data
                    ILog.e(YandexAdProvider.TAG, "adInfo=$data")
                    onAdLoadSuccess()
                }

                override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                    onAdLoadFailure("${adRequestError.code};${adRequestError.description}")
                }
            })
        }
        val adRequestConfiguration = AdRequestConfiguration.Builder(adProperty.adUnit).build()
        rewardedAdLoader?.loadAd(adRequestConfiguration)
    }

    override fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        super.show(activity, tag, placement, clientInfo)
        rewardedAd?.setAdEventListener(object : RewardedAdEventListener {
            override fun onAdClicked() {
                this@RewardedAdLoader.onAdClicked()
            }

            override fun onAdDismissed() {
                rewardedAd?.setAdEventListener(null)
                rewardedAd = null
                onAdClosed(gotReward)
            }

            override fun onAdFailedToShow(adError: AdError) {
                onAdShowFailed(adError.description)
            }

            override fun onAdImpression(impressionData: ImpressionData?) {
                onAdShowSuccess()
                impressionData?.rawData?.let { data ->
                    try {
                        val json = JSONObject(data)
                        val revenue: Double = json.getDouble("revenueUSD")
                        if (revenue >= 10.0 || revenue <= 0) {
                            return
                        }
                        onAdRevenuePaid(revenue, "USD")
                    } catch (_: Exception) {
                    }
                }
            }

            override fun onAdShown() {
ILog.e(YandexAdProvider.TAG, "ad shown")
            }

            override fun onRewarded(reward: Reward) {
                gotReward = true
                onUserRewarded()
            }

        })
        rewardedAd?.show(activity)
    }


}