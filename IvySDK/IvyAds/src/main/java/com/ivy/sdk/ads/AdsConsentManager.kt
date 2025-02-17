package com.ivy.sdk.ads

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform


internal class AdsConsentManager private constructor() {

    companion object {
        val Instance by lazy(LazyThreadSafetyMode.NONE) { AdsConsentManager() }
    }

    private var alreadyInvoke: Boolean = false

    fun checkGoogleUMP(activity: Activity, debug: Boolean, callback: () -> Unit) {
        val consentRequestParameters = when (debug) {
            true -> ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(
                    ConsentDebugSettings.Builder(activity)
                        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                        .addTestDeviceHashedId("F3EDE78A2C3C4127A07CA5E97F0FDD02")
                        .build()
                ).build()

            false -> ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
        }

        val consentInformation =
            UserMessagingPlatform.getConsentInformation(activity.applicationContext)

        if (consentInformation.canRequestAds() && !alreadyInvoke) {
            alreadyInvoke = true
            callback.invoke()
        }

        consentInformation.requestConsentInfoUpdate(activity, consentRequestParameters, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                if (consentInformation.canRequestAds() && !alreadyInvoke) {
                    alreadyInvoke = true
                    callback.invoke()
                }
            }
        }, { _ ->
            if (consentInformation.canRequestAds() && !alreadyInvoke) {
                alreadyInvoke = true
                callback.invoke()
            }
        })
    }

}