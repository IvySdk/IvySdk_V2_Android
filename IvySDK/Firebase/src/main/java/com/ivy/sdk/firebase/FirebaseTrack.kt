package com.ivy.sdk.firebase

import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.ivy.sdk.base.billing.PurchaseState
import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IConversationCallback
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

open class FirebaseTrack : AbsTrack() {

    companion object {
        const val TAG = "Firebase"
    }

    private var firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

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

        enableStatus = true
    }

    override fun setUserProperty(key: String, value: String) {
        try {
            if (key == "customer_user_id") {
                firebaseAnalytics.setUserProperty("cuid", value)
                return
            }
            if ("firebase_userId" == key) {
                firebaseAnalytics.setUserId(value)
                return
            }
            firebaseAnalytics.setUserProperty(key, value)
        } catch (e: Exception) {
            ILog.e(TAG, "set user property failed:$key;$value; reason:${e.message}")
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

    private fun logEvent(eventName: String, params: MutableMap<String, Any>) {
        when (enableStatus) {
            true -> {
                val bundle = params.let { map ->
                    val pairs = map.map { item -> Pair(item.key, item.value) }
                    return@let bundleOf(*pairs.toTypedArray())
                }
                firebaseAnalytics.logEvent(eventName, bundle)
                ILog.i(TAG, "$eventName sent")
            }

            false -> ILog.w(TAG, "send event:${eventName} failed;\nnot initialized!!!")
        }
    }

    private fun logAdRevenue(params: MutableMap<String, Any>) {
        when (enableStatus && enableAdPing) {
            true -> {
                params[EventParams.EVENT_PARAM_REVENUE]?.let { params[FirebaseAnalytics.Param.VALUE] = it }
                params.remove(EventParams.EVENT_PARAM_REVENUE)
                logEvent(EventIDs.AD_IMPRESSION_REVENUE_FIREBASE, params)
            }

            false -> ILog.w(TAG, "send ad revenue event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }

    private fun logPurchase(params: MutableMap<String, Any>) {
        when (enableStatus && enablePurchasePing) {
            true -> {
                params["price_amount"]?.let { params[FirebaseAnalytics.Param.VALUE] = it }
                params.remove("price_amount")
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
}