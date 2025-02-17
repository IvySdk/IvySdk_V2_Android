package com.ivy.sdk.ads

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.ivy.sdk.base.ads.AdProvider
import com.ivy.sdk.base.ads.AdType

internal class IvyBannerAd : IvyBaseAd() {


    override fun getAdType(): AdType = AdType.BANNER

    override fun isReady(): Boolean {
        for (adProperty in ads) {
            val status = helper?.getAdProvider(adProperty.platform.value)?.isBannerAdReady(adProperty.adUnit) ?: false
            if (status) {
                return true
            }
        }
        return false
    }

    override fun showAd(activity: Activity, tag: String, placement: Int, clientInfo: String?) {

    }

    override fun showBannerAd(adUnit: String, container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean {
        for (adProperty in ads) {
            if (adUnit != adProperty.adUnit) continue
            val adProvider = helper?.getAdProvider(adProperty.platform.value)
            val status = adProvider?.isBannerAdReady(adProperty.adUnit) ?: false
            if (status) {
                return adProvider?.showBannerAd(adUnit, container, tag, placement, clientInfo) ?: false
            }
        }
        return false
    }

    override fun closeBannerAd(adUnit: String, placement: Int) {
        super.closeBannerAd(adUnit, placement)
        for (adProperty in ads) {
            helper?.getAdProvider(adProperty.platform.value)?.closeBannerAd(adUnit, placement)
        }
    }

    override fun getBannerAdView(adUnit: String): View? {
        for (adProperty in ads) {
            if (adUnit != adProperty.adUnit) continue
            val adProvider = helper?.getAdProvider(adProperty.platform.value)
            val status = adProvider?.isBannerAdReady(adProperty.adUnit) ?: false
            if (status) {
                return adProvider?.getBannerAdView(adProperty.adUnit)
            }
        }
        return null
    }

    override fun isBannerAdReady(adUnit: String): Boolean {
        for (adProperty in ads) {
            if (adUnit != adProperty.adUnit) continue
            val adProvider = helper?.getAdProvider(adProperty.platform.value)
            return adProvider?.isBannerAdReady(adProperty.adUnit) ?: false
        }
        return false
    }

    override fun reloadBannerAd(adUnit: String?) {
        super.reloadBannerAd(adUnit)
        adUnit?.let { adId ->
            for (adProperty in ads) {
                if (adId != adProperty.adUnit) continue
                val adProvider = helper?.getAdProvider(adProperty.platform.value)
                adProvider?.loadBannerAd(adId)
            }
        } ?: run {
            for (adProperty in ads) {
                val adProvider = helper?.getAdProvider(adProperty.platform.value)
                adProvider?.loadBannerAd(adProperty.adUnit)
            }
        }

    }

    override fun onResume(container: FrameLayout?) {
        super.onResume(container)
        AdProvider.entries.forEach { adProvider ->
            helper?.getAdProvider(adProvider.value, false)?.onResume(container)
        }
    }

    override fun onPause(container: FrameLayout?) {
        super.onPause(container)
        AdProvider.entries.forEach { adProvider ->
            helper?.getAdProvider(adProvider.value, false)?.onPause(container)
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            AdProvider.entries.forEach { adProvider ->
                helper?.getAdProvider(adProvider.value, false)?.onDestroy()
            }
        } catch (_:Exception){}

    }

}