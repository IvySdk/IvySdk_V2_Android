package com.ivy.sdk.appsflyer

import android.content.Context
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLinkResult
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

internal class AppsflyerInit constructor(
    private var context: Context,
    private var config: String,
    private var debug: Boolean
) {

    companion object {
        const val TAG = "Appsflyer"
    }

    fun initAppsflyer(
        listener: AppsFlyerConversionListener,
        initResult: (status: Boolean) -> Unit
    ) {
        try {
            JSONObject(config).apply {
                val appKey = optString("app_key")
                if (appKey.isNullOrEmpty()) {
                    ILog.w(TAG, "app key invalid !!!")
                    initResult(false)
                    return
                }
                if (debug) {
                    AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.VERBOSE)
                    AppsFlyerLib.getInstance().setDebugLog(true)
                    AppsFlyerLib.getInstance().setMinTimeBetweenSessions(0)
                }

                val inviterTemplateId = optString("inviter_template_id");
                if (!inviterTemplateId.isNullOrEmpty()) {
                    AppsFlyerLib.getInstance().setAppInviteOneLink(inviterTemplateId)
                    AppsFlyerLib.getInstance().subscribeForDeepLink { result ->
                        when (result.status) {
                            DeepLinkResult.Status.ERROR -> ILog.e(TAG, "deep link err:\n${result.error}")
                            DeepLinkResult.Status.NOT_FOUND -> ILog.i(TAG, "deep link not found")
                            DeepLinkResult.Status.FOUND -> {
                                result.deepLink?.let { deepLink ->
                                    ILog.i(TAG, "deep link:$deepLink;\n deferred status: ${deepLink.isDeferred}")
                                    deepLink.clickEvent?.let { clickEvent ->
                                        var inviterUserId = clickEvent.optString("deep_link_sub1")
                                        if (inviterUserId.isNullOrEmpty()) {
                                            ILog.i(TAG, "not valid inviter user id ")
                                            return@subscribeForDeepLink
                                        }
                                        val currentUserId = LocalStorage.Instance.decodeString("invite_current_user_id", "-")
                                        when (currentUserId.equals(inviterUserId)) {
                                            true -> {
                                                ILog.i(TAG, "user id equal inviter id, same user")
                                            }

                                            false -> {
                                                ILog.i(TAG, "inviter user id:${inviterUserId}")
                                                val inviterAppId = clickEvent.optString("deep_link_sub2")
                                                if (inviterAppId.isNullOrEmpty()) {
                                                    inviterUserId = "${inviterUserId}|${inviterAppId}"
                                                }
                                                LocalStorage.Instance.encodeString("af_invite_id", inviterUserId)
                                            }
                                        }
                                    } ?: ILog.i(TAG, "deep link click event is null")
                                } ?: ILog.i(TAG, "deep link is null")
                            }
                        }
                    }
                }
                AppsFlyerLib.getInstance().init(appKey, listener, context)
                AppsFlyerLib.getInstance().start(context, appKey, object : AppsFlyerRequestListener {
                    override fun onSuccess() {
                        ILog.i(TAG, "init success")
                        initResult(true)
                    }

                    override fun onError(p0: Int, p1: String) {
                        ILog.e(TAG, "init failed:\n error code: ${p0};\n error msg:${p1}")
                        initResult(false)
                    }
                })
            }
        } catch (e: Exception) {
            ILog.e(TAG, "init failed:${e.message}")
            initResult(false)
        }
    }

}