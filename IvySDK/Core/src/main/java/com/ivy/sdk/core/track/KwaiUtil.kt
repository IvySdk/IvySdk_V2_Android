package com.ivy.sdk.core.track

import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IKeys

//快手海外事件
open class KwaiUtil {

    open fun checkAdRevenue(revenue: Double) {
        val totalRevenue = LocalStorage.Instance.decodeDouble(IKeys.KEY_KWAI_TOTAL_AD_REVENUE, 0.0) + revenue
        LocalStorage.Instance.encodeDouble(IKeys.KEY_KWAI_TOTAL_AD_REVENUE, totalRevenue)
        if (totalRevenue >= 0.05 && !LocalStorage.Instance.containsKey("cpm_total_0_05")) {
            LocalStorage.Instance.encodeBoolean("cpm_total_0_05", true)
            val params = mutableMapOf<String, Any>("kwai_key_event_action_type" to 2, "kwai_key_event_action_value" to 0.05)
            IvyTrack.Instance.logEvent("cpm_total_0_05", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        }

        if (totalRevenue >= 0.1 && !LocalStorage.Instance.containsKey("cpm_total_0_1")) {
            LocalStorage.Instance.encodeBoolean("cpm_total_0_1", true)
            val params = mutableMapOf<String, Any>("kwai_key_event_action_type" to 2, "kwai_key_event_action_value" to 0.1)
            IvyTrack.Instance.logEvent("cpm_total_0_1", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        }

        if (totalRevenue >= 0.05 && !LocalStorage.Instance.containsKey("revenue_total_day0_0_05")) {
            LocalStorage.Instance.encodeBoolean("revenue_total_day0_0_05", true)
            val params = mutableMapOf<String, Any>("kwai_key_event_action_type" to 4, "kwai_key_event_action_value" to 0.05)
            IvyTrack.Instance.logEvent("revenue_total_day0_0_05", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        }

        if (totalRevenue >= 0.1 && !LocalStorage.Instance.containsKey("revenue_total_day0_0_1")) {
            LocalStorage.Instance.encodeBoolean("revenue_total_day0_0_1", true)
            val params = mutableMapOf<String, Any>("kwai_key_event_action_type" to 4, "kwai_key_event_action_value" to 0.1)
            IvyTrack.Instance.logEvent("revenue_total_day0_0_1", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        }
    }

    open fun checkAdEvents(eventName: String, params: MutableMap<String, Any>?) {
        val showTimes = LocalStorage.Instance.decodeInt(IKeys.KEY_KWAI_AD_SHOW_TIMES, 0) + 1
        LocalStorage.Instance.encodeInt(IKeys.KEY_KWAI_AD_SHOW_TIMES, showTimes)
        if (showTimes == 1) {
            logAdShowEvent(1, 1);
        }
        if (showTimes == 3) {
            logAdShowEvent(1, 3);
        }
        if (showTimes == 5) {
            logAdShowEvent(1, 5);
        }
        if (showTimes == 10) {
            logAdShowEvent(1, 10);
        }
    }

    private fun logAdShowEvent(type: Int, times: Int) {
        val params = mutableMapOf<String, Any>("kwai_key_event_action_type" to type, "kwai_key_event_action_value" to times)
        IvyTrack.Instance.logEvent("ad_show$times", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
    }


}