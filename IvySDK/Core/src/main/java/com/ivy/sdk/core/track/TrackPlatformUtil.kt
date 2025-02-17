package com.ivy.sdk.core.track

import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog


class TrackPlatformUtil {

    companion object {
        fun instantiateTrackPlatform(platform: TrackPlatform): AbsTrack? {
            return when (platform) {
                TrackPlatform.NONE -> {
                    ILog.w("TrackPlatformUtil", "none platform find for ${platform.value}")
                    null
                }

                TrackPlatform.FIREBASE -> {
                    try {
                        return (Class.forName("com.ivy.sdk.firebase.FirebaseTrack")
                            .getDeclaredConstructor().newInstance() as? AbsTrack)
                    } catch (e: Exception) {
                        ILog.e(IvyTrack.TAG, "instance platform:${platform} failed;${e.message ?: ""}")
                    }
                    null
                }

                TrackPlatform.FACEBOOK -> {
                    try {
                        return (Class.forName("com.ivy.sdk.facebook.FacebookTrack")
                            .getDeclaredConstructor()
                            .newInstance() as? AbsTrack)
                    } catch (e: Exception) {
                        ILog.e(IvyTrack.TAG, "instance platform:${platform} failed;${e.message ?: ""}")
                    }
                    null
                }

                TrackPlatform.APPSFLYER -> {
                    try {
                        return (Class.forName("com.ivy.sdk.appsflyer.AppsflyerTrack")
                            .getDeclaredConstructor()
                            .newInstance() as? AbsTrack)
                    } catch (e: Exception) {
                        ILog.e(IvyTrack.TAG, "instance platform:${platform} failed;${e.message ?: ""}")
                    }
                    null
                }

                TrackPlatform.THINkING_DATA -> {
                    try {
                        return (Class.forName("ivy.data.analytics.ThinkingDataTrack")
                            .getDeclaredConstructor()
                            .newInstance() as? AbsTrack)
                    } catch (e: Exception) {
                        ILog.e(IvyTrack.TAG, "instance platform:${platform} failed;${e.message ?: ""}")
                    }
                    null
                }
            }
        }

        fun getTrackPlatform(v: Int): TrackPlatform {
            return when (v) {
                0 -> TrackPlatform.NONE
                1 -> TrackPlatform.FACEBOOK
                2 -> TrackPlatform.FIREBASE
                3 -> TrackPlatform.APPSFLYER
                4 -> TrackPlatform.THINkING_DATA
                else -> TrackPlatform.NONE
            }
        }

    }

}