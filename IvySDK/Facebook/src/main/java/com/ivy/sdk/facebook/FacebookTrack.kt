package com.ivy.sdk.facebook

import android.content.Context
import androidx.core.os.bundleOf
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.ivy.sdk.base.billing.PurchaseState
import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IConversationCallback
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Currency

open class FacebookTrack : AbsTrack() {

    companion object {
        const val TAG = "Facebook"
    }

    private lateinit var appEventsLogger: AppEventsLogger

    override fun setup(
        context: Context,
        appId: String,
        config: String,
        roleId: String,
        debug: Boolean,
        conversationCallback: IConversationCallback?
    ) {
        super.setup(context, appId, config, roleId, debug, conversationCallback)
        try {
            JSONObject(config).apply {
                enableAdPing = optBoolean("enableAdPing", true)
                enablePurchasePing = optBoolean("enablePurchasePing", true)
            }
        } catch (_: Exception) {
        }
        when (debug) {
            true -> {
                FacebookSdk.setIsDebugEnabled(true)
                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
            }

            false -> {
                FacebookSdk.setIsDebugEnabled(false)
            }
        }
        appEventsLogger = AppEventsLogger.newLogger(context)
        enableStatus = true
    }

    override fun setUserProperty(key: String, value: String) {

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
        when (enableStatus) {
            true -> {
                val bundle = params.let { map ->
                    val pairs = map.map { item -> Pair(item.key, item.value) }
                    return@let bundleOf(*pairs.toTypedArray())
                }
                appEventsLogger.logEvent(eventName, bundle)
            }

            false -> ILog.w(TAG, "send $eventName failed;\nnot initialized!!!")
        }
    }

    private fun logAdRevenue(params: MutableMap<String, Any>) {
        when (enableStatus && enableAdPing) {
            true -> {
                (params[EventParams.EVENT_PARAM_REVENUE] as? Double)?.let { revenue ->
                    val bundle = params.let { map ->
                        val pairs = map.map { item -> Pair(item.key, item.value) }
                        return@let bundleOf(*pairs.toTypedArray())
                    }

                    appEventsLogger.logEvent(AppEventsConstants.EVENT_NAME_AD_IMPRESSION, revenue, bundle)
                }
            }

            false -> ILog.w(TAG, "send ad revenue event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }

    private fun logPurchase(params: MutableMap<String, Any>) {
        when (enableStatus && enablePurchasePing) {
            true -> {
                val price = params["price_amount"] as? Double ?: return
                val currency = params["currency"] as? String ?: "USD"
                val state = params[EventParams.EVENT_PARAM_PAY_STATE]
                when (state) {
                    PurchaseState.PRE_ORDER -> {
//                        val pairs = params.map { item -> Pair(item.key, item.value) }
//                        val bundle = bundleOf(*pairs.toTypedArray())
//                        appEventsLogger.logEvent(EventIDs.IAP_PRE_ORDER, bundle)
                    }
                    PurchaseState.VERIFICATION -> {
//                        val pairs = params.map { item -> Pair(item.key, item.value) }
//                        val bundle = bundleOf(*pairs.toTypedArray())
//                        appEventsLogger.logEvent(EventIDs.IAP_VERIFICATION, bundle)
                    }
                    PurchaseState.PAY_RESULT -> {
                        val pairs = params.map { item -> Pair(item.key, item.value) }
                        val bundle = bundleOf(*pairs.toTypedArray())
                        appEventsLogger.logPurchase(BigDecimal(price), Currency.getInstance(currency), bundle)
                    }
                    else -> {}
                }
            }

            false -> ILog.w(TAG, "send purchase event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }


}