package ivy.data.analytics

import android.content.Context
import android.icu.util.TimeZone
import com.ivy.sdk.base.billing.PurchaseState
import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IConversationCallback
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

class ThinkingDataTrack : AbsTrack() {

    companion object {
        const val TAG = "ThinkingData"
    }

    override fun setup(
        context: Context,
        appId: String,
        config: String,
        roleId: String,
        debug: Boolean,
        conversationCallback: IConversationCallback?
    ) {
        super.setup(context, appId, config, roleId, debug, conversationCallback)
        JSONObject(config).apply {
            enableAdPing = optBoolean("enableAdPing", true)
            enablePurchasePing = optBoolean("enablePurchasePing", true)
            val appKey = optString("app_key")
            val serverUrl = optString("server_url")

            if (appKey.isNullOrEmpty() || serverUrl.isNullOrEmpty()) {
                ILog.e(TAG, "init failed; appKey:${appKey}; server_url:${serverUrl}")
                return
            }
            val tdConfig = TDConfig.getInstance(context, appKey, serverUrl)
            /**
             * NORMAL       模式:数据会存入缓存，并依据一定的缓存策略上报,默认为NORMAL模式；建议在线上环境使用
             * Debug        模式:数据逐条上报。当出现问题时会以日志和异常的方式提示用户；不建议在线上环境使用
             * DebugOnly    模式:只对数据做校验，不会入库；不建议在线上环境使用
             */
            tdConfig.setMode(
                when (debug) {
                    true -> {
                        TDAnalytics.enableLog(true)
                        TDConfig.TDMode.DEBUG
                    }

                    false -> TDConfig.TDMode.NORMAL
                }
            )
            TDAnalytics.init(tdConfig)

//            TDAnalytics.enableAutoTrack(
//                TDAnalytics.TDAutoTrackEventType.APP_INSTALL or
//                        TDAnalytics.TDAutoTrackEventType.APP_START or
//                        TDAnalytics.TDAutoTrackEventType.APP_END or
//                        TDAnalytics.TDAutoTrackEventType.APP_CRASH
//            ) { eventType, properties ->
//                ILog.i(TAG, "auto track event cached;\n${eventType}")
//                return@enableAutoTrack properties ?: JSONObject()
//            }
            enableStatus = true
        }
    }

    override fun setUserProperty(key: String, value: String) {
        when (key) {
            "distinct_id" -> TDAnalytics.setDistinctId(value)
            "login_id" -> TDAnalytics.login(value)
            "customer_user_id" -> TDAnalytics.getSuperProperties().apply { put("cuid", value) }
            else -> TDAnalytics.getSuperProperties().apply { put(key, value) }
        }
    }

    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {
        val fixedParams = params?.toMutableMap() ?: mutableMapOf()
        fixedParams[EventParams.EVENT_PARAM_EVENT_SRC] = eventSrc
        roleId?.let { fixedParams[EventParams.EVENT_PARAM_ROLE_ID] = it }
        when (eventType) {
            EventType.EVENT_TYPE_PURCHASE -> {
                logPurchase(fixedParams)
            }

            EventType.EVENT_TYPE_AD_REVENUE -> {
                logAdRevenue(fixedParams)
            }

            else -> {
                logEvent(eventName, fixedParams)
            }
        }
    }

    private fun logEvent(eventName: String, params: Map<String, Any>) {
        try {
            when (enableStatus) {
                true -> {
                    val jsonParams = map2JSONObject(params)
                    TDAnalytics.track(eventName, JSONObject().put("extra", jsonParams))
                }

                false -> ILog.w(TAG, "send event:${eventName} failed;\nnot initialized!!!")
            }
        } catch (e: Exception) {
            ILog.e(TAG, "send event:${eventName} failed;${e.message}")
        }
    }

    private fun logAdRevenue(params: Map<String, Any>) {
        try {
            when (enableStatus && enableAdPing) {
                true -> logEvent(EventIDs.AD_IMPRESSION_REVENUE, params)

                false -> ILog.w(TAG, "send ad revenue event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
            }
        } catch (e: Exception) {
            ILog.e(TAG, "send ad revenue event failed;${e.message}")
        }
    }

    private fun logPurchase(params: MutableMap<String, Any>) {
        when (enableStatus && enablePurchasePing) {
            true -> {
                val state = params[EventParams.EVENT_PARAM_PAY_STATE]
                when (state) {
                    PurchaseState.PRE_ORDER -> logEvent(EventIDs.IAP_PRE_ORDER, params)
                    PurchaseState.VERIFICATION -> logEvent(EventIDs.IAP_VERIFICATION, params)
                    PurchaseState.PAY_RESULT -> logEvent(EventIDs.IAP_PURCHASED, params)
                    else -> {}
                }
            }

            false -> ILog.w(TAG, "send purchase event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }

    private fun map2JSONObject(params: Map<String, Any>): JSONObject {
        val json = JSONObject()
        try {
            params.forEach {
                try {
                    if (it.value is Boolean) {
                        if (it.value as Boolean) {
                            json.put(it.key, 1)
                        } else {
                            json.put(it.key, 0)
                        }
                    } else {
                        json.put(it.key, it.value)
                    }
                } catch (_: Exception) {
                    ILog.e(TAG, "event param value:${it.value} for key:${it.key} invalid!!!")
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "format event params err:${e.message}")
        }
        return json
    }

}