package com.ivy.sdk.core.track

import android.content.Context
import com.ivy.sdk.base.App
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.AbsTrack
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IConversationCallback
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.track.ITrack
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.IvyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

data class EventTask(
    val eventName: String,
    val eventType: String,
    val eventSrc: String,
    val params: MutableMap<String, Any>?,
    val platforms: List<TrackPlatform>?
)

open class IvyTrack private constructor() : ITrack() {

    // 统计平台
    private var trackPlatforms: MutableMap<TrackPlatform, AbsTrack?> =
        mutableMapOf(*mutableListOf<Pair<TrackPlatform, AbsTrack?>>().also {
            TrackPlatform.entries.forEach { tp ->
                it.add(Pair<TrackPlatform, AbsTrack?>(tp, null))
            }
        }.toTypedArray())

    // 事件流向
    private var eventChannelUtil: EventChannelUtil? = null

    //组合事件
    private var combinedEventUtil: CombineUtil? = null

    //累加事件
    private var accumulateEventUtil: AccumulateUtil? = null

    //测试模式
    private var debug: Boolean = false

    //首次运行时间戳
    private var firstOpenTime: Long = 0L
    private var alreadyReceivedAfConversion = false

    private var isTrackPlatformsReady: Boolean = false
    private val waitingEventTasks: MutableList<EventTask> = mutableListOf()

    companion object {
        const val TAG = "IvyTrack"
        const val ONE_DAY_MILLISECONDS = 24 * 60 * 60 * 1000
        val Instance by lazy(LazyThreadSafetyMode.NONE) { IvyTrack() }
    }

    /**
     * @param context   ApplicationContext
     * @param config    default.json内 track 内容
     * @param debug
     */
    override fun setup(context: Context, appId:String, config: String, roleId: String, debug: Boolean, conversationCallback: IConversationCallback?) {
        this.debug = debug
        setupFirstOpenTime()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                JSONObject(config).let { base ->
                    base.optJSONObject("platform")?.let {
                        setupTrackPlatforms(it, appId, roleId)
                        setUserProperty("role_id", roleId)
                        isTrackPlatformsReady = true
                    } ?: ILog.e(TAG, "load track platform failed!!!")

                    base.optJSONObject("combinedEvents")?.let {
                        if (combinedEventUtil == null) combinedEventUtil = CombineUtil()
                        combinedEventUtil?.setup(it)
                    } ?: ILog.e(TAG, "load combined events failed!!!")

                    base.optJSONObject("accumulateEvent")?.let {
                        if (accumulateEventUtil == null) accumulateEventUtil = AccumulateUtil()
                        accumulateEventUtil?.setup(it)
                    } ?: ILog.e(TAG, "load accumulate events failed!!!")

                    base.optJSONObject("eventChannel")?.let {
                        if (eventChannelUtil == null) eventChannelUtil = EventChannelUtil()
                        eventChannelUtil?.setup(it)
                    } ?: ILog.e(TAG, "load event channel failed!!!")
                    recordAppOpen()
                    //打点设备信息
                    loadDeviceInfo()
                }
            } catch (e: Exception) {
                ILog.e(TAG, "setup track failed:${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun setupTrackPlatforms(json: JSONObject, appId:String, roleId: String) {
        json.keys().forEach { key ->
            when (key) {
                TrackPlatform.FACEBOOK.value -> {
                    json.optString(key).let { config ->
                        TrackPlatformUtil.instantiateTrackPlatform(TrackPlatform.FACEBOOK)?.let { absTrack ->
                                absTrack.setup(App.Instance, appId,config, roleId, debug)
                                trackPlatforms[TrackPlatform.FACEBOOK] = absTrack
                            }
                    }
                }

                TrackPlatform.FIREBASE.value -> {
                    json.optString(key).let { config ->
                        TrackPlatformUtil.instantiateTrackPlatform(TrackPlatform.FIREBASE)?.let { absTrack ->
                                absTrack.setup(App.Instance, appId, config, roleId, debug)
                                trackPlatforms[TrackPlatform.FIREBASE] = absTrack
                            }
                    }
                }

                TrackPlatform.APPSFLYER.value -> {
                    json.optString(key).let { config ->
                        TrackPlatformUtil.instantiateTrackPlatform(TrackPlatform.APPSFLYER)?.let { absTrack ->
                                absTrack.setup(App.Instance, appId, config, roleId, debug, object : IConversationCallback {
                                    override fun onConversionDataSuccess(var1: Map<String, Any>?) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            when (alreadyReceivedAfConversion) {
                                                true -> ILog.e(TAG, "received af conversion once again:${var1?.toList()?.toTypedArray()}")

                                                false -> {
                                                    var1?.let { map ->
                                                        Instance.logEvent(
                                                            EventIDs.SDK_FIRST_AF_CONVERSION, EventType.EVENT_TYPE_COMMON,
                                                            EventSrc.EVENT_SRC_SDK, map.toMutableMap(), listOf(TrackPlatform.THINkING_DATA)
                                                        )
                                                        val status: String? = map["af_status"]?.toString()
                                                        when (!status.isNullOrEmpty() && status != "Organic") {
                                                            true -> {
                                                                map["media_source"]?.toString()?.let {
                                                                    Instance.setUserProperty("media_source", it)
                                                                    LocalStorage.Instance.encodeString(IKeys.KEY_AF_MEDIA_SOURCE, it)
                                                                }
                                                                map["campaign"]?.toString()?.let {
                                                                    Instance.setUserProperty("campaign_id", it)
                                                                    LocalStorage.Instance.encodeString(IKeys.KEY_AF_CAMPAIGN_ID, it)
                                                                }
                                                                map["af_adset"]?.toString()?.let { Instance.setUserProperty("af_adset", it) }
                                                            }

                                                            false -> Instance.setUserProperty("af_campaign", "Organic")
                                                        }
                                                    }
                                                }
                                            }
                                            alreadyReceivedAfConversion = true
                                        }
                                    }

                                    override fun onConversionDataFail(var1: String?) {

                                    }

                                    override fun onAppOpenAttribution(var1: Map<String, String>?) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            var1?.let {
                                                Instance.logEvent(
                                                    EventIDs.SDK_FIRST_AF_APP_OPEN_ATTRIBUTION, EventType.EVENT_TYPE_COMMON,
                                                    EventSrc.EVENT_SRC_SDK, it.toMutableMap(), listOf(TrackPlatform.THINkING_DATA)
                                                )
                                            }
                                        }
                                    }

                                    override fun onAttributionFailure(var1: String?) {

                                    }
                                })
                                trackPlatforms[TrackPlatform.APPSFLYER] = absTrack
                                getAppsflyerId()?.let { setUserProperty("appsflyer_id", it) }
                            }
                    }
                }

                TrackPlatform.THINkING_DATA.value -> {
                    json.optString(key).let { config ->
                        TrackPlatformUtil.instantiateTrackPlatform(TrackPlatform.THINkING_DATA)?.let { absTrack ->
                            absTrack.setup(App.Instance, appId, config, roleId, debug)
                            trackPlatforms[TrackPlatform.THINkING_DATA] = absTrack
                            getAppsflyerId()?.let { setUserProperty("appsflyer_id", it) }

                            LocalStorage.Instance.decodeString(IKeys.KEY_AF_CAMPAIGN_ID)?.let {
                                Instance.setUserProperty("campaign_id", it)
                            }
                            LocalStorage.Instance.decodeString(IKeys.KEY_AF_MEDIA_SOURCE)?.let {
                                Instance.setUserProperty("media_source", it)
                            }
                        }
                    }
                }

                else -> ILog.w(TAG, "don't find platform:${key}")
            }
        }
    }

    private fun checkEventPlatform(eventName: String): List<TrackPlatform>? =
        eventChannelUtil?.getTrackPlatform(eventName)

    override fun setUserProperty(key: String, value: String) {
        trackPlatforms.mapValues { platform ->
            platform.value?.setUserProperty(key, value)
        }
    }

    fun setUserProperty(key: String, value: String, platform: TrackPlatform) {
        trackPlatforms[platform]?.setUserProperty(key, value)
    }

    private fun doLogEvent(
        eventName: String,
        eventType: String,
        eventSrc: String,
        params: MutableMap<String, Any>?,
        platforms: List<TrackPlatform>? = null,
        checkCustomizeEvent: Boolean = true,
    ) {
        //检测事件流向并打点, 优先使用配置文件中的控制条件
        checkEventPlatform(eventName)?.let { plats ->
            //配置文件中的流向
            plats.forEach { item ->
                trackPlatforms[item]?.logEvent(eventName, eventType, eventSrc, params, platforms)
            }
        } ?: run {
            platforms?.forEach {
                //打点指定的流向
                trackPlatforms[it]?.logEvent(eventName, eventType, eventSrc, params)
            } ?: run {
                //默认流向所有平台
                trackPlatforms.values.forEach { it?.logEvent(eventName, eventType, eventSrc, params, platforms) }
            }
        }
        if (eventSrc == EventSrc.EVENT_SRC_COMBINATION) {
            //组合事件，不再重复组合
            return
        }
        if (checkCustomizeEvent) {
            combinedEventUtil?.check(eventName, params)
            accumulateEventUtil?.check(eventName, params)
        }
    }

    override fun appsflyerInviteUser(channel: String, campaign: String, inviterId: String, inviterAppId: String) {
        trackPlatforms[TrackPlatform.APPSFLYER]?.appsflyerInviteUser(channel, campaign, inviterId, inviterAppId)
    }

    override fun getAppsflyerInviterId(): String? =
        trackPlatforms[TrackPlatform.APPSFLYER]?.getAppsflyerInviterId()

    override fun getAppsflyerId(): String? = trackPlatforms[TrackPlatform.APPSFLYER]?.getAppsflyerId()

    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {
        if (!isTrackPlatformsReady) {
            recordEvent(eventName, eventType, eventSrc, params, platforms)
            return
        }
        doLogEvent(eventName, eventType, eventSrc, params, platforms)
        if (isTrackPlatformsReady) {
            deRecordEvent()
        }
    }

    private fun setupFirstOpenTime() {
        if (LocalStorage.Instance.containsKey(IKeys.KEY_FIRST_OPEN_TIME)) {
            this.firstOpenTime = LocalStorage.Instance.decodeLong(IKeys.KEY_FIRST_OPEN_TIME, System.currentTimeMillis())
        } else {
            this.firstOpenTime = System.currentTimeMillis()
            LocalStorage.Instance.encodeLong(IKeys.KEY_FIRST_OPEN_TIME, this.firstOpenTime)
        }
    }

    open fun getFirstOpenTime(): Long {
        if (firstOpenTime == 0L) {
            setupFirstOpenTime()
        }
        return this.firstOpenTime
    }

    private fun recordAppOpen() {
        val openTimes = LocalStorage.Instance.decodeInt(IKeys.KEY_APP_START_TIMES, 0) + 1
        if (openTimes == 1) {
            logEvent(EventIDs.SDK_FIRST_APP_OPEN, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK)
        }
        LocalStorage.Instance.encodeInt(IKeys.KEY_APP_START_TIMES, openTimes)
        logEvent(EventIDs.EVENT_APP_OPEN, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, mutableMapOf<String, Any>("times" to openTimes))
        accumulateEventUtil?.checkAppOpen(openTimes)
    }

    private fun loadDeviceInfo() {
        IvyUtil.loadDeviceInfo()?.let {
            logEvent(EventIDs.DEVICE_INFO, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, it)
        }
    }

    private fun recordEvent(
        eventName: String,
        eventType: String,
        eventSrc: String,
        params: MutableMap<String, Any>?,
        platforms: List<TrackPlatform>?
    ) {
        ILog.i(TAG, "record event:$eventName")
        EventTask(eventName, eventType, eventSrc, params, platforms).apply {
            waitingEventTasks.add(this)
        }
    }

    private fun deRecordEvent() {
        try {
            if (waitingEventTasks.isEmpty()) return
            val it = waitingEventTasks.iterator()
            while (it.hasNext()) {
                val eventTask = it.next()
                ILog.i(TAG, "de-record event:${eventTask.eventName}")
                doLogEvent(eventTask.eventName, eventTask.eventType, eventTask.eventSrc, eventTask.params, eventTask.platforms)
            }
            waitingEventTasks.clear()
        } catch (_: Exception) {
        }
    }


}