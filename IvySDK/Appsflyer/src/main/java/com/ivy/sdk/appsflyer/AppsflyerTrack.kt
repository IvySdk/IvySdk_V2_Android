package com.ivy.sdk.appsflyer

import android.content.Context
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.share.LinkGenerator
import com.appsflyer.share.ShareInviteHelper
import com.ivy.sdk.base.App
import com.ivy.sdk.base.billing.PurchaseState
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IConversationCallback
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ActivityUtil

import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.IvyUtil
import org.json.JSONObject

open class AppsflyerTrack : AbsTrack() {

    private val userProperties: MutableMap<String, String> = mutableMapOf()

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
        AppsflyerInit(context, config, debug).initAppsflyer(listener = object :
            AppsFlyerConversionListener {
            override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                p0?.let {
                    ILog.i(AppsflyerInit.TAG, "onConversionDataSuccess: ")
                    it.forEach { data-> ILog.i(AppsflyerInit.TAG, "${data.key}:${data.value}") }
                } ?: ILog.i(AppsflyerInit.TAG, "onConversionDataSuccess: empty !!!")
                conversationCallback?.onConversionDataSuccess(p0)
            }

            override fun onConversionDataFail(p0: String?) {
                ILog.i(AppsflyerInit.TAG, "onConversionDataFail:${p0 ?: ""}")
                conversationCallback?.onConversionDataFail(p0)
            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                p0?.let {
                    ILog.i(AppsflyerInit.TAG, "onAppOpenAttribution: ")
                    it.forEach { data-> ILog.i(AppsflyerInit.TAG, "${data.key}:${data.value}") }
                } ?: ILog.i(AppsflyerInit.TAG, "onAppOpenAttribution: empty !!!")
                conversationCallback?.onAppOpenAttribution(p0)
            }

            override fun onAttributionFailure(p0: String?) {
                ILog.i(AppsflyerInit.TAG, "onAttributionFailure:${p0 ?: ""}")
                conversationCallback?.onAttributionFailure(p0)
            }

        }, initResult = { status: Boolean ->
//            enableStatus = status
            ILog.i(AppsflyerInit.TAG, "init result:$status")
        })
    }

    override fun setUserProperty(key: String, value: String) {
      //  if (enableStatus) {
            if (key == "customer_user_id") {
                AppsFlyerLib.getInstance().setCustomerUserId(value)
            } else {
                userProperties[key] = value
            }
       // }
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

    override fun getAppsflyerId(): String? = AppsFlyerLib.getInstance().getAppsFlyerUID(App.Instance)

    private fun logEvent(eventName: String, params: MutableMap<String, Any>) {
        when (enableStatus) {
            true -> {
                try {
                    params.putAll(userProperties)
                } catch (_: Exception) {
                }
                AppsFlyerLib.getInstance().logEvent(context, eventName, params, object : AppsFlyerRequestListener {
                    override fun onSuccess() {
                        ILog.i(AppsflyerInit.TAG, "$eventName send success")
                    }

                    override fun onError(errorCode: Int, errorDesc: String) {
                        ILog.e(AppsflyerInit.TAG, "$eventName sent failed:$errorCode;$errorDesc")
                    }
                })
            }

            false -> ILog.w(AppsflyerInit.TAG, "send event:$eventName failed; not initialized")
        }
    }

    private fun logAdRevenue(params: MutableMap<String, Any>) {
        when (enableStatus && enableAdPing) {
            true -> {
                params[EventParams.EVENT_PARAM_CURRENCY]?.let { params["af_currency"] = it }
                params[EventParams.EVENT_PARAM_REVENUE]?.let { params["af_revenue"] = it }
                params[EventParams.EVENT_PARAM_AD_MEDIATION]?.let { params["mediationNetwork"] = it }
                params[EventParams.EVENT_PARAM_AD_NETWORK]?.let { params["monetizationNetwork"] = it }
                try {
                    params.putAll(userProperties)
                } catch (_: Exception) {
                }
                AppsFlyerLib.getInstance().logEvent(context, "af_ad_revenue", params, object : AppsFlyerRequestListener {
                    override fun onSuccess() {
                        ILog.i(AppsflyerInit.TAG, "af_ad_revenue send success")
                    }

                    override fun onError(errorCode: Int, errorDesc: String) {
                        ILog.e(AppsflyerInit.TAG, "af_ad_revenue sent failed:\n$errorCode;$errorDesc")
                    }
                })
            }

            false -> ILog.w(AppsflyerInit.TAG, "send ad revenue event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }

    private fun logPurchase(params: MutableMap<String, Any>) {
        try {
            params.putAll(userProperties)
        } catch (_: Exception) {
        }
        when (enableStatus && enablePurchasePing) {
            true -> {
                val state = params[EventParams.EVENT_PARAM_PAY_STATE]

                when (state) {
                    PurchaseState.PRE_ORDER -> logEvent(EventIDs.IAP_PRE_ORDER, params)
                    PurchaseState.VERIFICATION -> logEvent(EventIDs.IAP_VERIFICATION, params)
                    PurchaseState.PAY_RESULT -> {
                        if (params["state"] == 1) {
                            params["price_amount"]?.let { params[AFInAppEventParameterName.REVENUE] = it }
                            params["sku"]?.let { params[AFInAppEventParameterName.CONTENT_ID] = it }
                            params["type"]?.let { params[AFInAppEventParameterName.CONTENT_TYPE] = it }
                            params["currency"]?.let { params[AFInAppEventParameterName.CURRENCY] = it } ?: run { params["currency"] = "USD" }

                            AppsFlyerLib.getInstance().logEvent(context, AFInAppEventType.PURCHASE, params, object : AppsFlyerRequestListener {
                                override fun onSuccess() {
                                    ILog.i(AppsflyerInit.TAG, "log purchase success send success")
                                }

                                override fun onError(errorCode: Int, errorDesc: String) {
                                    ILog.e(AppsflyerInit.TAG, "log purchase success failed to be sent:\n$errorCode;$errorDesc")
                                }
                            })
                        }
                    }
                    else -> {}
                }
            }

            false -> ILog.w(AppsflyerInit.TAG, "send purchase event failed;\nenableStatus:$enableStatus;enableAdPing:$enableAdPing")
        }
    }

    override fun appsflyerInviteUser(channel: String, campaign: String, inviterId: String, inviterAppId: String) {
        super.appsflyerInviteUser(channel, campaign, inviterId, inviterAppId)
        ActivityUtil.Instance.activity?.let { activity ->
            LocalStorage.Instance.encodeString("invite_current_user_id", inviterId)
            val linkGenerator = ShareInviteHelper.generateInviteUrl(context).also {
                it.addParameter("deep_link_sub1", inviterId)
                it.addParameter("deep_link_sub2", inviterAppId)
                it.setChannel("android_user_invite")
                it.setCampaign("user_invite")
                it.addParameter("af_sub1", inviterId)
            }
            linkGenerator.generateLink(context, object : LinkGenerator.ResponseListener {
                override fun onResponse(p0: String?) {
                    p0?.let { it ->
                        IvyUtil.systemShareText(activity, it)
                        val params = mutableMapOf<String, String>().also { p ->
                            p["referrerId"] = inviterId
                            p["campaign"] = "user_invite"
                            p["channel"] = "android_user_invite"
                            roleId?.let { v -> p["roleId"] = v }
                        }
                        ShareInviteHelper.logInvite(context, "android_user_invite", params)
                    } ?: ILog.e(AppsflyerInit.TAG, "generator share url failed:\n empty url")
                }

                override fun onResponseError(p0: String?) {
                    ILog.e(AppsflyerInit.TAG, "generator share url failed:\nerr:${p0}")
                }
            })
        } ?: ILog.e(AppsflyerInit.TAG, "generator share url failed: current activity invalid")
    }

    override fun getAppsflyerInviterId(): String? =
        LocalStorage.Instance.decodeString("af_invite_id")


}