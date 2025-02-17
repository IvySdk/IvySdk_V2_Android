package com.ivy.sdk.base.ads

import com.ivy.sdk.base.track.IEvent
import com.ivy.sdk.base.track.TrackPlatform

abstract class IAdListener: IEvent {

    abstract fun onAdLoadSuccess(adType: AdType)

    abstract fun onAdLoadFailure(adType: AdType, reason: String? = null)

    abstract fun onAdShowSuccess(adType: AdType, tag: String, placement: Int)

    abstract fun onAdShowFailed(adType: AdType, reason: String? = null, tag: String, placement: Int)

    abstract fun onAdClicked(adType: AdType, tag: String, placement: Int)

    abstract fun onAdClosed(adType: AdType, gotReward: Boolean = false, tag: String, placement: Int)

    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {

    }

}