package com.ivy.sdk.aihelp

import com.ivy.sdk.base.App
import com.ivy.sdk.base.helper.IHelperCallback
import com.ivy.sdk.base.helper.IIHelper
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.aihelp.config.ApiConfig
import net.aihelp.config.UserConfig
import net.aihelp.config.enums.ShowConversationMoment
import net.aihelp.event.AsyncEventListener
import net.aihelp.event.EventType
import net.aihelp.init.AIHelpSupport
import org.json.JSONObject

open class AIHelpImpl : IIHelper {

    companion object {
        const val TAG = "AIHelp"
    }

    private var callback: IHelperCallback? = null
    private var hasInitialized: Boolean = false
    private var listenUnreadMessage: Boolean = false
    private var currentMessageCount: Int = 0
    private var unreadMessageLoopDuration: Long = 5 * 60 * 1000

    private val initEventListener = AsyncEventListener { jsonEventData, _ ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(jsonEventData)
                hasInitialized = json.getBoolean("isSuccess")
                if (!hasInitialized) {
                    val message = json.optString("message")
                    ILog.w(TAG, "init err:$message")
                } else {
                    login()
                    AIHelpSupport.fetchUnreadMessageCount()
                }
                unreadMessageLoopDuration = json.optLong("unreadMessageLoopDuration", 300) * 1000
            } catch (e: Exception) {
                ILog.e(TAG, "init response err:${e.message}")
            }
            AIHelpSupport.unregisterAsyncEventListener(EventType.INITIALIZATION)
        }
    }

    private val messageEventListener = AsyncEventListener { jsonEventData, _ ->
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val json = JSONObject(jsonEventData)
                currentMessageCount = json.optInt("msgCount", 0)
                ILog.i(TAG, "current unread message count:$currentMessageCount")
                callback?.onUnreadHelperMessageCount(currentMessageCount)
            } catch (e: Exception) {
                ILog.e(TAG, "listen message err:${e.message}")
            }
        }
    }

    private val sessionClosedEventListener = AsyncEventListener { jsonEventData, _ ->
        CoroutineScope(Dispatchers.Main).launch {
            ILog.i(TAG, "session closed! fetch unread message count")
            AIHelpSupport.fetchUnreadMessageCount()
        }
    }

    override fun setup(data: String, debug: Boolean, callback: IHelperCallback) {
//        if (USER_FROM_MAINLAND_CHINA) {
//            AIHelpSupport.additionalSupportFor(PublishCountryOrRegion.CN);
//        }
        try {
            AIHelpSupport.enableLogging(debug)

            JSONObject(data).let { config ->
                val appId = config.getString("appId")
                val domain = config.getString("domain")
                unreadMessageLoopDuration =
                    config.optLong("unreadMessageLoopDuration", 5 * 60 * 1000L)
                // val appKey = config.getString("appKey")
                AIHelpSupport.registerAsyncEventListener(
                    EventType.INITIALIZATION,
                    initEventListener
                )
                AIHelpSupport.registerAsyncEventListener(
                    EventType.MESSAGE_ARRIVAL,
                    messageEventListener
                )
                AIHelpSupport.registerAsyncEventListener(
                    EventType.SESSION_CLOSE,
                    sessionClosedEventListener
                )
                AIHelpSupport.initialize(App.Instance, domain, appId)

            }
        } catch (e: Exception) {
            ILog.e(TAG, "setup failed:${e.message}")
        }
    }

    override fun isHelperInitialized(): Boolean = hasInitialized

    override fun hasNewHelperMessage(): Boolean = currentMessageCount > 0

    override fun showHelper(
        entranceId: String,
        meta: String?,
        tags: String?,
        welcomeMessag: String?
    ) {
        updateHelperUserInfo(meta, tags)
        ApiConfig.Builder().run {
            setEntranceId(entranceId)
            welcomeMessag?.let { setWelcomeMessage(it) }
            AIHelpSupport.show(build())
        }
    }

    override fun showHelperSingleFAQ(faqId: String, moment: Int) {
        if (moment == 0) {
            AIHelpSupport.showSingleFAQ(faqId, ShowConversationMoment.NEVER)
        } else if (moment == 1) {
            AIHelpSupport.showSingleFAQ(faqId, ShowConversationMoment.AFTER_MARKING_UNHELPFUL)
        } else if (moment == 2) {
            AIHelpSupport.showSingleFAQ(faqId, ShowConversationMoment.ONLY_IN_ANSWER_PAGE)
        } else {
            AIHelpSupport.showSingleFAQ(faqId, ShowConversationMoment.ALWAYS)
        }
    }

    override fun listenHelperUnreadMessageCount(onlyOnce: Boolean) {
        listenUnreadMessage = true
        AIHelpSupport.fetchUnreadMessageCount()
        if (!onlyOnce) {
            loopUnreadMessageCount()
        }
    }

    override fun stopListenHelperUnreadMessageCount() {
        listenUnreadMessage = false
    }

    override fun updateHelperUserInfo(data: String?, tags: String?) {
        with(UserConfig.Builder()) {
            data?.let { setCustomData(it) }
            tags?.let { setUserTags(tags) }
            AIHelpSupport.updateUserInfo(build())
        }
    }

    override fun resetHelperUserInfo() {
        AIHelpSupport.resetUserInfo()
    }

    override fun closeHelper() {
        AIHelpSupport.unregisterAsyncEventListener(EventType.MESSAGE_ARRIVAL)
        AIHelpSupport.close()
    }

    private fun loopUnreadMessageCount() {
        CoroutineScope(Dispatchers.Default).launch {
            while (listenUnreadMessage) {
                delay(unreadMessageLoopDuration)
                AIHelpSupport.fetchUnreadMessageCount()
            }
        }
    }

    private fun login() {
        AIHelpSupport.login(Util.roleId())
    }


}