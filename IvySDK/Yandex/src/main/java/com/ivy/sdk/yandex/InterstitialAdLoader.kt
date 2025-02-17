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
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import org.json.JSONObject

internal class InterstitialAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) : IAdLoader(
    adProperty, adConfig, pamManager, adListener
) {

    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdLoader: InterstitialAdLoader? = null

    override fun isReady(): Boolean = interstitialAd != null

    override fun doLoadAd() {
        val adRequestConfiguration = AdRequestConfiguration.Builder(adProperty.adUnit).build()
        interstitialAdLoader = InterstitialAdLoader(App.Instance).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdFailedToLoad(error: AdRequestError) {
                    onAdLoadFailure("${error.code};${error.description}")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    val data = ad.info.data
                    ILog.e(YandexAdProvider.TAG, "adInfo=$data")
                    onAdLoadSuccess()
                }
            })
        }
        interstitialAdLoader?.loadAd(adRequestConfiguration)
    }

    override fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        super.show(activity, tag, placement, clientInfo)
        interstitialAd?.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdClicked() {
                this@InterstitialAdLoader.onAdClicked()
            }

            override fun onAdDismissed() {
                interstitialAd?.setAdEventListener(null)
                interstitialAd = null
                onAdClosed(false)
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

        })
        interstitialAd?.show(activity)
    }


}