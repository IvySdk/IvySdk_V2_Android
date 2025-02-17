package com.ivy.sdk.base.ads

import com.ivy.sdk.base.track.IEvent
import com.ivy.sdk.base.track.TrackPlatform

interface IInternalAdListener : IEvent {

    fun onAdLoadSuccess()

    fun onAdLoadFailure(reason: String? = null)

    fun onAdShowSuccess()

    fun onAdShowFailed(reason: String? = null)

    fun onAdClicked()

    fun onAdClosed(gotReward: Boolean = false)

    fun onUserRewarded()

    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {

    }
}