package com.ivy.sdk.base.ads

import org.json.JSONObject

/**
 * 广告平台
 */
enum class AdProvider(val value: String) {
    INVALID("invalid"),
    ADMOB("admob"),
    APPLOVIN_MAX("applovinmax"),
    YANDEX("yandex");


    companion object {
        fun retrieve(value: String?): AdProvider? {
            return when (value) {
                ADMOB.value -> ADMOB
                APPLOVIN_MAX.value -> APPLOVIN_MAX
                YANDEX.value -> YANDEX
                else -> null
            }
        }
    }
}

/**
 * 广告单元配置
 */
class AdProperty private constructor() {
    lateinit var adUnit: String
    var priority: Int = 0
    lateinit var adType: AdType
    var adaptive: Boolean = false
    var collapsible: String? = null
    var platform: AdProvider = AdProvider.INVALID
    var apsAdUnit: String = ""

    companion object {
        fun decode(type: AdType, plat: AdProvider, json: JSONObject?): AdProperty? = json?.let {
            AdProperty().apply {
                platform = plat
                if (platform == AdProvider.INVALID) return@let null
                adUnit = it.optString("placement", "") ?: ""
                if (adUnit.isEmpty()) return@let null
                priority = it.optInt("priority", 1)
                adaptive = it.optBoolean("adaptive", false)
                collapsible = it.optString("collapsible", "") ?: ""
                apsAdUnit = it.optString("aps_placement", "") ?: ""
                adType = type
            }
        }
    }
}

/**
 * 广告全局控制性属性
 */
class AdConfig private constructor() {
    var delayOnLoadFail: Int = 2
    var timesDelayOnLoadFail: Int = 5
    var adLoadTimeOut: Int = 10
    var bannerAdRefreshDuration: Long = 2000
    var bannerRefreshByPlatform: Boolean = false

    companion object {
        fun decode(json: JSONObject?): AdConfig = AdConfig().also { c ->
            json?.let {
                c.delayOnLoadFail = it.optInt("delayOnLoadFail", 2)
                c.timesDelayOnLoadFail = it.optInt("timesDelayOnLoadFail", 5)
                c.adLoadTimeOut = it.optInt("adLoadTimeOut", 60)
                c.bannerAdRefreshDuration = it.optLong("bannerAdRefreshDuration", 2000L)
                c.bannerRefreshByPlatform = it.optBoolean("bannerRefreshByPlatform", false)
            }
        }



    }



}