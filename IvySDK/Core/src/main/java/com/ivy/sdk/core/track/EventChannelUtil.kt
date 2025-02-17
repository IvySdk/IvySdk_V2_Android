package com.ivy.sdk.core.track

import com.ivy.sdk.base.track.TrackPlatform
import org.json.JSONObject


/**
 *     "event_channel": {
 *       "banner_displayed": "0000"
 *     }
 *
 *     配置方式：
 *     0000：共4位，分别对应四个打点平台； 0： 关闭，1：开启
 *     第1位： appsflyer
 *     第2位： firebase
 *     第3位： facebook
 *     第4位： thing data
 *
 *     示例：如事件只 流向firebase，配置位 ”0100“
 *
 */
class EventChannelUtil {

    private var channels: MutableMap<String, List<TrackPlatform>> = mutableMapOf()

//    private var allPlatforms: List<TrackPlatform> = listOf(
//        TrackPlatform.FACEBOOK,
//        TrackPlatform.FIREBASE,
//        TrackPlatform.APPSFLYER,
//        TrackPlatform.THINkING_DATA
//    )

    fun setup(json: JSONObject) {
        json.keys().forEach { eventName ->
            json.optString(eventName).let { conf ->
                val list = mutableListOf<TrackPlatform>()
                channels[eventName] = list
                val confs = conf.split("")
                for ((index, value) in confs.withIndex()) {
                    if (value == "1") {
                        when (index) {
                            1 -> list.add(TrackPlatform.APPSFLYER)
                            2 -> list.add(TrackPlatform.FIREBASE)
                            3 -> list.add(TrackPlatform.FACEBOOK)
                            4 -> list.add(TrackPlatform.THINkING_DATA)
                        }
                    }
                }
            }
        }
    }

    fun getTrackPlatform(event: String): List<TrackPlatform>? = channels[event]

}