package com.ivy.sdk.ads

import android.app.Activity
import com.ivy.sdk.base.ads.AdType

internal class IvyInterstitialAd : IvyBaseAd() {

    override fun getAdType(): AdType = AdType.INTERSTITIAL

    override fun isReady(): Boolean {
        for (adProperty in ads) {
            val status = helper?.getAdProvider(adProperty.platform.value)?.isInterstitialAdReady(adProperty.adUnit) ?: false
            if (status) {
                return true
            }
        }
        return false
    }

    override fun showAd(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        for (adProperty in ads) {
            val adProvider = helper?.getAdProvider(adProperty.platform.value)
            val status = adProvider?.isInterstitialAdReady(adProperty.adUnit) ?: false
            if (status) {
                adProvider?.showInterstitialAd(adProperty.adUnit, tag, placement, clientInfo)
                return
            }
        }
        adListener?.onAdShowFailed(AdType.INTERSTITIAL, "no ad prepared", tag, placement)
    }
}