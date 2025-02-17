package com.ivy.sdk.ads

import android.app.Activity
import com.ivy.sdk.base.ads.AdType

internal class IvyRewardedAd : IvyBaseAd() {
    override fun getAdType(): AdType = AdType.REWARDED

    override fun isReady(): Boolean {
        for (adProperty in ads) {
            val status = helper?.getAdProvider(adProperty.platform.value)?.isRewardedAdReady(adProperty.adUnit) ?: false
            if (status) {
                return true
            }
        }
        return false
    }

    override fun showAd(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        for (adProperty in ads) {
            val adProvider = helper?.getAdProvider(adProperty.platform.value)
            val status = adProvider?.isRewardedAdReady(adProperty.adUnit) ?: false
            if (status) {
                adProvider?.showRewardedAd(adProperty.adUnit, tag, placement, clientInfo)
                return
            }
        }
        adListener?.onAdShowFailed(AdType.REWARDED, "no ad prepared", tag, placement)
    }


}