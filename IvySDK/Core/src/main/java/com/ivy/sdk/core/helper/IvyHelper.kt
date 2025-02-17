package com.ivy.sdk.core.helper

import com.ivy.sdk.base.helper.IHelperCallback
import com.ivy.sdk.base.helper.IIHelper
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

class IvyHelper : IIHelper {

    companion object {
        const val TAG = "Customer"
    }

    private var helper: IIHelper? = null

    override fun setup(data: String, debug: Boolean, callback: IHelperCallback) {
        try {
            val json = JSONObject()
            if (json.has("AIHelp")) {
                val config = json.getString("AIHelp")
                helper = Class.forName("com.ivy.sdk.aihelp.AIHelpImpl").getDeclaredConstructor().newInstance() as? IIHelper
                helper?.setup(config, debug, callback)
            }
        } catch (e: Exception) {
            ILog.e(TAG, "setup customer err:${e.message}")
        }
    }

    override fun isHelperInitialized(): Boolean {
        return helper?.isHelperInitialized() ?: run {
            ILog.w(TAG, "customer service not setup!!!")
            false
        }
    }

    override fun hasNewHelperMessage(): Boolean {
        return helper?.hasNewHelperMessage() ?: run {
            ILog.w(TAG, "customer service not setup!!!")
            false
        }
    }

    override fun showHelper(
        entranceId: String,
        meta: String?,
        tags: String?,
        welcomeMessag: String?
    ) {
        helper?.showHelper(entranceId, meta, tags, welcomeMessag) ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun showHelperSingleFAQ(faqId: String, moment: Int) {
        helper?.showHelperSingleFAQ(faqId, moment) ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun listenHelperUnreadMessageCount(onlyOnce: Boolean) {
        helper?.listenHelperUnreadMessageCount(onlyOnce) ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun stopListenHelperUnreadMessageCount() {
        helper?.stopListenHelperUnreadMessageCount() ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun updateHelperUserInfo(data: String?, tags: String?) {
        helper?.updateHelperUserInfo(data, tags) ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun resetHelperUserInfo() {
        helper?.resetHelperUserInfo() ?: ILog.w(TAG, "customer service not setup!!!")
    }

    override fun closeHelper() {
        helper?.closeHelper() ?: ILog.w(TAG, "customer service not setup!!!")
    }


}