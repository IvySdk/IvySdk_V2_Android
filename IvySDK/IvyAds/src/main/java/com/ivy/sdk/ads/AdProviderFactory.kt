package com.ivy.sdk.ads

import com.ivy.sdk.base.ads.AdProvider
import com.ivy.sdk.base.ads.IAdProvider
import com.ivy.sdk.base.utils.ILog

internal class AdProviderFactory {

    companion object {

        fun instantiate(adProvider: String): IAdProvider? {
            var classFullName: String? = null
            when (adProvider) {
                AdProvider.ADMOB.value -> classFullName = "com.ivy.sdk.admob.AdmobAdProvider"
                AdProvider.APPLOVIN_MAX.value -> classFullName = "com.ivy.sdk.max.MaxAdProvider"
                AdProvider.YANDEX.value -> classFullName = "com.ivy.sdk.yandex.YandexAdProvider"
                else -> {}
            }
            if (classFullName != null) {
                try {
                    return Class.forName(classFullName)
                        .getDeclaredConstructor()
                        .newInstance() as? IAdProvider
                } catch (e: Exception) {
                    ILog.e(IvyAds.TAG, "instantiate ad provider:$adProvider:${e.message ?: ""}")
                }
            }
            ILog.e(IvyAds.TAG, "unable instantiate ad provider:$adProvider")
            return null
        }
    }

}