package com.ivy.sdk.admob

import android.app.Activity
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.utils.ILog

internal class InterstitialAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) :
    IAdLoader(adProperty, adConfig, pamManager, adListener),
    OnPaidEventListener {

    private var interstitialAd: InterstitialAd? = null
    private var taskId: Int = 0

    private var lastRevenue: Double = 0.0

    override fun doLoadAd() {
        try {
            val adRequest = AdRequest.Builder()
            (pamManager?.getPAM(AdType.INTERSTITIAL, lastRevenue) as? PAM)?.let { pam ->
                val bundle = Bundle().apply {
                    val pamBundle = Bundle().apply { putString("userGroup", pam.value) }
                    putBundle("admob_custom_keyvals", pamBundle)
                }
                adRequest.addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
            }
            InterstitialAd.load(App.Instance, adProperty.adUnit, adRequest.build(), object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    onAdLoadFailure("${p0.code};${p0.message}")
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    interstitialAd = p0
                    interstitialAd?.onPaidEventListener = this@InterstitialAdLoader
                    try {
                        p0.responseInfo.loadedAdapterResponseInfo?.let { resp ->
                            eventParams[EventParams.EVENT_PARAM_AD_MEDIATION] = resp.adSourceName
                            eventParams[EventParams.EVENT_PARAM_AD_SOURCE_INSTANCE] = resp.adSourceInstanceName
                        }

                        val extra = p0.responseInfo.responseExtras
                        eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_GROUP] = extra.getString("mediation_group_name", "")
                        eventParams[EventParams.EVENT_PARAM_AD_MEDIATION_AB_TEST] = extra.getString("mediation_ab_test_name", "")
                    } catch (_: Exception) {

                    }
                    onAdLoadSuccess()
                }
            })
        } catch (e: Exception) {
            onAdLoadFailure("${e.message}")
        }
    }

    override fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        super.show(activity, tag, placement, clientInfo)
        interstitialAd?.let { ad ->
            this@InterstitialAdLoader.taskId = taskId
            ad.fullScreenContentCallback = this@InterstitialAdLoader.fullScreenContentCallback
            ad.show(activity)
        } ?: onAdShowFailed("invalid ad")
    }

    override fun isReady(): Boolean = interstitialAd != null

    override fun onPaidEvent(adValue: AdValue) {
        ILog.i(TAG, "${adProperty.adType.value} paid event:")
        ILog.i(TAG, "ad unit:${adProperty.adUnit}")
        ILog.i(TAG, "value:${adValue.valueMicros};currency:${adValue.currencyCode}")
        val revenue: Double = (adValue.valueMicros / 1000000.0f).toDouble()
        if (revenue >= 10.0 || revenue <= 0) {
            return
        }
        lastRevenue = revenue
        onAdRevenuePaid(revenue, adValue.currencyCode)
    }

    private val fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            onAdShowSuccess()
        }

        override fun onAdClicked() {
            super.onAdClicked()
            this@InterstitialAdLoader.onAdClicked()
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            interstitialAd = null
            onAdClosed(false)
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            super.onAdFailedToShowFullScreenContent(p0)
            interstitialAd = null
            onAdShowFailed("${p0.code};${p0.message}")
        }

    }



}