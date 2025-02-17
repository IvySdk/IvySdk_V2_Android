package com.ivy.sdk.core

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.Observer
import com.ivy.sdk.ads.IvyAds
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.billing.IIPurchaseResult
import com.ivy.sdk.base.billing.IPurchase
import com.ivy.sdk.base.billing.IPurchaseResult
import com.ivy.sdk.base.firebase.IFirebase

import com.ivy.sdk.base.game.archive.IArchiveResult
import com.ivy.sdk.base.game.auth.AuthPlatforms
import com.ivy.sdk.base.game.auth.IAuthResponse
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.game.auth.IFirebaseAuthReload
import com.ivy.sdk.base.game.auth.IFirebaseUnlink
import com.ivy.sdk.base.grid.GridManager
import com.ivy.sdk.base.grid.IGridQuery
import com.ivy.sdk.base.helper.IHelper
import com.ivy.sdk.base.helper.IHelperCallback
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IEvent
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.track.TrackPlatform
import com.ivy.sdk.base.utils.ActivityUtil

import com.ivy.sdk.base.utils.AppStoreUtil
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.IToast
import com.ivy.sdk.base.utils.IvyUtil
import com.ivy.sdk.base.utils.Util
import com.ivy.sdk.core.helper.IvyHelper
import com.ivy.sdk.core.notch.NotchCompat
import com.ivy.sdk.core.notification.NotificationUtil
import com.ivy.sdk.core.payment.IvyPay
import com.ivy.sdk.core.track.IvyTrack
import com.ivy.sdk.core.track.KwaiUtil
import com.ivy.sdk.core.track.UacUtil
import com.ivy.sdk.core.utils.Helper
import com.ivy.sdk.games.IvyGames
import com.ivy.sdk.remote.config.IvyRemoteConfig
import org.json.JSONObject

class IvySdk private constructor() : IEvent, IPurchase, IGridQuery, IHelper {

    companion object {
        const val TAG = "IvySdk"
        val Instance by lazy(LazyThreadSafetyMode.NONE) { IvySdk() }
    }

    private var debug: Boolean = false
    private var appId: String = ""
    private var useNotch: Boolean = false

    private var ivyGames: IvyGames? = null
    private var ivyHelper: IvyHelper? = null
    private var ivyPay: IvyPay? = null
    private var builder: Builder? = null

    private var firebaseImpl: IFirebase? = null

    //uac
    private var uacUtil: UacUtil? = null
    private var adPingThreshold: Double = 0.1

    //快手
    private var kwaiUtil: KwaiUtil? = null

    fun onCreate(activity: Activity, builder: Builder) {
        ActivityUtil.Instance.activity = activity
        this.builder = builder
        loadMetaConfig()
        setupFirebase()
        checkNotchSetting()
        onNewIntent(ActivityUtil.Instance.activity?.intent)
        ILog.i(TAG, "start do onCreate")
        GridManager.Instance.addListener(observer = object : Observer<JSONObject?> {
            override fun onChanged(value: JSONObject?) {
                ILog.i(TAG, "received app config data:$value")
                if (value != null) {
                    GridManager.Instance.removeListener(this)
                    GridManager.Instance.parseConfig(object : GridManager.IGrid() {
                        override fun onGameServices(data: String?) {
                            super.onGameServices(data)
                            ILog.i(TAG, "game services config:")
                            ILog.i(TAG, ": $data")
                            data?.let {
                                if (ivyGames == null) {
                                    ivyGames = IvyGames()
                                }
                                ivyGames?.setup(appId, data, authResult, debug)
                                ivyGames?.preCheckLastLoginStatus()
                            } ?: ILog.w(TAG, "no game services configured")
                        }

                        override fun onPayment(data: String?) {
                            super.onPayment(data)
                            ILog.i(TAG, "payment config:")
                            ILog.i(TAG, ": $data")
                            data?.let {
                                if (ivyPay == null) ivyPay = IvyPay()
                                ivyPay?.setup(appId, data, debug, this@IvySdk.purchaseResponseListener)
                            } ?: ILog.w(TAG, "payment config invalid")
                        }
                    })

                }
            }
        })
    }

    fun onStart() {

    }

    fun onResume(activity: Activity) {
        IvyAds.Instance.onResume()
    }

    fun onPause() {
        IvyAds.Instance.onPause()
    }

    fun onStop() {

    }

    fun onDestroy() {
        IvyAds.Instance.onDestroy()
    }

    fun onNewIntent(intent: Intent?) {
        intent?.getStringExtra("src_action")?.let { action ->
            ILog.i(TAG, "received notification event:$action")
            builder?.notificationEvent?.onReceivedNotificationAction(action)
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ivyGames?.onActivityResult(requestCode, resultCode, data)
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        NotificationUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun loadNotificationPermissionState(): Int = NotificationUtil.loadNotificationPermissionState()

    fun requestNotificationPermission() = NotificationUtil.requestNotificationPermission()

    fun openNotificationSettings() = NotificationUtil.openNotificationSettings()

    fun pushNotificationTask(
        tag: String,
        title: String,
        subtitle: String?,
        bigText: String?,
        smallIcon: String?,
        largeIcon: String?,
        bigPicture: String?,
        delay: Long,
        autoCancel: Boolean,
        action: String?,
        repeat: Boolean,
        requireNetwork: Boolean,
        requireCharging: Boolean
    ) {
        NotificationUtil.addNotificationTask(
            tag, title, subtitle, bigText, smallIcon, largeIcon, bigPicture, delay,
            autoCancel, action, repeat, requireNetwork, requireCharging
        )
    }

    fun cancelNotification(tag: String?) = tag?.let { NotificationUtil.cancelTask(it) } ?: NotificationUtil.cancelAllTask()

    /**
     *  设置顺序
     *  1. 初始化配置参数、运行环境、本地缓存实例
     *  2. 初始化 firebase remote config
     *  3. 初始化 GridData : 回调中优先处理 顺序 1、事件平台  2、广告  3、计费 4、客服、5、推送 6、其它...
     */
    fun setupConfig() {
        ILog.i(TAG, "setupConfig")
        //读取meta配置
        loadMetaConfig()
        ILog.i(TAG, "loaded meta data")
        //初始化firebase app
        setupFirebase()
        ILog.i(TAG, "firebase setup ")
        IvyRemoteConfig.Instance.check(appId, 5 * 60, debug, appConfigCallback = { json ->
            ILog.i(GridManager.TAG, "load remote app config success")
            GridManager.Instance.setData(json)
            GridManager.Instance.addListener(observer = object : Observer<JSONObject?> {
                override fun onChanged(value: JSONObject?) {
                    ILog.i(TAG, "received data:$value")
                    GridManager.Instance.removeListener(this)
                    GridManager.Instance.parseConfig(object : GridManager.IGrid() {
                        override fun onTrackConfig(data: String?) {
                            // 优先初始化打点平台，尽量快获取 归因数据
                            ILog.i(TAG, "track config:")
                            ILog.i(TAG, ": $data")
                            data?.let {
                                IvyTrack.Instance.setup(App.Instance, appId, it, Util.roleId(), debug)
                            } ?: ILog.w(TAG, "track platform config invalid")
                            if (LocalStorage.Instance.contains(IKeys.KEY_PAYING_USER)) {
                                setUserProperty("paying_user", "1", TrackPlatform.THINkING_DATA)
                            }
                        }

                        override fun onCustomerServiceConfig(data: String?) {
                            super.onCustomerServiceConfig(data)
                            //客服配置
                            data?.let {
                                if (ivyHelper == null) ivyHelper = IvyHelper()
                                ivyHelper?.setup(data, debug, customerEvents)
                            } ?: ILog.w(TAG, "customer config invalid")
                        }

                        override fun onGridRemoteConfig(data: String?) {
                            super.onGridRemoteConfig(data)
                            ILog.i(TAG, "grid remote config config:")
                            ILog.i(TAG, ": $data")
                            data?.let { v ->
                                try {
                                    val map: MutableMap<String, Any> = mutableMapOf()
                                    val rcJson = JSONObject(v)
                                    rcJson.keys().forEach { key ->
                                        map[key] = rcJson.opt(key) ?: ""
                                    }
                                    IvyRemoteConfig.Instance.setDefaultData(map)
                                } catch (e: Exception) {
                                    ILog.e(TAG, "set default remote config failed:${e.message}")
                                }
                            }
                            setupDefaultRemoteData(data)
                        }

                        override fun onData() {
                            super.onData()
                            initUac()
                            adPingThreshold = GridManager.Instance.getGridConfig("adPingThreshold", 0.1)
                            initKwai()
                        }

                        override fun onAdConfig(data: String?) {
                            super.onAdConfig(data)
                            //缓存广告配置，在OnCreate之后初始化
                            ILog.i(TAG, "ad config:")
                            ILog.i(TAG, ": $data")
                            data?.let {
                                IvyAds.Instance.setup(it, debug, IvyRemoteConfig.Instance, adListener)
                            } ?: ILog.w(TAG, "ad config invalid")
                        }
                    })
                }
            })
        }, remoteConfigCallback = {
            ILog.e(GridManager.TAG, "load remote config success")
        })
    }

    // =========================== 广告 =================================
    val adListener = object : IAdListener() {
        override fun onAdLoadSuccess(adType: AdType) {
            ILog.i(TAG, "${adType.value} load success")
            builder?.adListener?.onAdLoadSuccess(adType)
        }

        override fun onAdLoadFailure(adType: AdType, reason: String?) {
            ILog.i(TAG, "${adType.value} load failed:${reason}")
            builder?.adListener?.onAdLoadFailure(adType, reason)
        }

        override fun onAdShowSuccess(adType: AdType, tag: String, placement: Int) {
            ILog.i(TAG, "${adType.value} show success")
            builder?.adListener?.onAdShowSuccess(adType, tag, placement)
        }

        override fun onAdShowFailed(adType: AdType, reason: String?, tag: String, placement: Int) {
            ILog.i(TAG, "${adType.value} show failed $reason")
            builder?.adListener?.onAdShowFailed(adType, reason, tag, placement)
        }

        override fun onAdClicked(adType: AdType, tag: String, placement: Int) {
            ILog.i(TAG, "${adType.value} clicked")
            builder?.adListener?.onAdClicked(adType, tag, placement)
        }

        override fun onAdClosed(adType: AdType, gotReward: Boolean, tag: String, placement: Int) {
            ILog.i(TAG, "${adType.value} closed")
            builder?.adListener?.onAdClosed(adType, gotReward, tag, placement)
        }
        override fun logEvent(
            eventName: String,
            eventType: String,
            eventSrc: String,
            params: MutableMap<String, Any>?,
            platforms: List<TrackPlatform>?
        ) {
            super.logEvent(eventName, eventType, eventSrc, params, platforms)
            this@IvySdk.logEvent(eventName, eventType, eventSrc, params, platforms)
        }
    }

    fun hasRewardedAd() = IvyAds.Instance.isAvailable(AdType.REWARDED)

    fun showRewardedAd(tag: String, placement: Int = 0, clientInfo: String? = null) {
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "06", EventParams.EVENT_PARAM_AD_FORMAT to AdType.REWARDED.value)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placement
        if (!clientInfo.isNullOrEmpty()) {
            try {
                val clientInfos = mutableMapOf<String, Any>()
                val json = JSONObject(clientInfo)
                for (key in json.keys()) {
                    val value = json.get(key)
                    if (value is Boolean) {
                        clientInfos[key] = if (value) 1 else 0
                    } else {
                        clientInfos[key] = value
                    }
                }
                params.putAll(clientInfos)
            } catch (e: Exception) {
                ILog.e(IAdLoader.TAG, "format client info failed:${e.message}")
            }
        }
        logEvent(EventIDs.AD_REACH_PAGE, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        IvyAds.Instance.showAd(AdType.REWARDED, tag, placement, clientInfo)
    }

    fun hasInterstitialAd() = IvyAds.Instance.isAvailable(AdType.INTERSTITIAL)

    fun showInterstitialAd(tag: String, placement: Int = 0, clientInfo: String? = null) {
        val params =
            mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "06", EventParams.EVENT_PARAM_AD_FORMAT to AdType.INTERSTITIAL.value)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placement
        if (!clientInfo.isNullOrEmpty()) {
            try {
                val clientInfos = mutableMapOf<String, Any>()
                val json = JSONObject(clientInfo)
                for (key in json.keys()) {
                    val value = json.get(key)
                    if (value is Boolean) {
                        clientInfos[key] = if (value) 1 else 0
                    } else {
                        clientInfos[key] = value
                    }
                }
                params.putAll(clientInfos)
            } catch (e: Exception) {
                ILog.e(IAdLoader.TAG, "format client info failed:${e.message}")
            }
        }
        logEvent(EventIDs.AD_REACH_PAGE, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        IvyAds.Instance.showAd(AdType.INTERSTITIAL, tag, placement, clientInfo)
    }

    fun hasBannerAd() = IvyAds.Instance.isAvailable(AdType.BANNER)

    fun showBannerAd(position: Int, tag: String, placement: Int, clientInfo: String?) {
        val params = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_FLOW_SEQ to "06", EventParams.EVENT_PARAM_AD_FORMAT to AdType.BANNER.value)
        params[EventParams.EVENT_PARAM_AD_PLACEMENT] = placement
        if (!clientInfo.isNullOrEmpty()) {
            try {
                val clientInfos = mutableMapOf<String, Any>()
                val json = JSONObject(clientInfo)
                for (key in json.keys()) {
                    val value = json.get(key)
                    if (value is Boolean) {
                        clientInfos[key] = if (value) 1 else 0
                    } else {
                        clientInfos[key] = value
                    }
                }
                params.putAll(clientInfos)
            } catch (e: Exception) {
                ILog.e(IAdLoader.TAG, "format client info failed:${e.message}")
            }
        }
        logEvent(EventIDs.AD_REACH_PAGE, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        IvyAds.Instance.showBannerAd(position, tag, placement, clientInfo)
    }

    fun closeBannerAd(placement: Int) = IvyAds.Instance.closeBannerAd(placement)
// =========================== 广告 =================================

    // =========================== 事件 ================================
    override fun logEvent(eventName: String, eventType: String, eventSrc: String, params: MutableMap<String, Any>?, platforms: List<TrackPlatform>?) {
        IvyTrack.Instance.logEvent(eventName, eventType, eventSrc, params, platforms)
        if (eventType == EventType.EVENT_TYPE_AD_REVENUE) {
            collectAdRevenue(params)
            kwaiUtil?.let { util ->
                params?.let { p ->
                    (p[EventParams.EVENT_PARAM_REVENUE] as? Double)?.let { util.checkAdRevenue(it) }
                }
            }
        }
        if (eventType == EventType.EVENT_TYPE_COMMON && eventSrc == EventSrc.EVENT_SRC_SDK && eventName == EventIDs.AD_SHOW_SUCCESS) {
            kwaiUtil?.checkAdEvents(eventName, params)
        }
    }

    private fun collectAdRevenue(params: MutableMap<String, Any>?) {
        params?.let { src ->
            (src[EventParams.EVENT_PARAM_REVENUE] as? Double)?.let { revenue ->
                val adValue = LocalStorage.Instance.decodeDouble(IKeys.KEY_AD_ROAS, 0.0) + revenue
                if (adValue >= adPingThreshold) {
                    val eventParams = mutableMapOf<String, Any>(EventParams.EVENT_PARAM_CURRENCY to "USD", EventParams.EVENT_PARAM_VALUE to adValue)
                    logEvent(EventIDs.GMS_AD_PAID, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, eventParams)
                    LocalStorage.Instance.encodeDouble(IKeys.KEY_AD_ROAS, 0.0)
                } else {
                    LocalStorage.Instance.encodeDouble(IKeys.KEY_AD_ROAS, adValue)
                }

                val totalRevenue = LocalStorage.Instance.decodeDouble(IKeys.KEY_TOTAL_AD_REVENUE, 0.0) + revenue
                LocalStorage.Instance.encodeDouble(IKeys.KEY_TOTAL_AD_REVENUE, totalRevenue)
                checkUac(revenue.toFloat(), totalRevenue.toFloat())
            }
        }
    }

    fun setUserProperty(key: String, value: String, platform: TrackPlatform?) {
        platform?.let {
            IvyTrack.Instance.setUserProperty(key, value, it)
        } ?: IvyTrack.Instance.setUserProperty(key, value)
    }

    //uac
    private fun initUac() {
        if (uacUtil == null) {
            val uacUrl = GridManager.Instance.getGridConfig("uacApi", "")
            if (uacUrl.isNullOrEmpty()) return
            val appId = Helper.getConfig(Helper.CONFIG_KEY_APP_ID) ?: ""
            uacUtil = UacUtil(appId, uacUrl).apply { checkAndUpdateUacTop() }
        }
    }

    private fun checkUac(revenue: Float, totalRevenue: Float) {
        uacUtil?.checkUacLtvConversion(revenue, totalRevenue)
        uacUtil?.checkFirstThreeDaysLTV(totalRevenue)
    }
//uac

    //快手
    private fun initKwai() {
//        val kwaiEventsStatus = GridManager.Instance.getGridConfig("kwaiEvents", true)
//        if (kwaiEventsStatus) {
//            kwaiUtil = KwaiUtil()
//        }
    }

// =========================== 事件 ================================

    private fun loadMetaConfig() {
        try {
            if (debug){
                return
            }
            val applicationInfo = App.Instance.packageManager.getApplicationInfo(App.Instance.packageName, PackageManager.GET_META_DATA)
            debug = applicationInfo.metaData.getBoolean("ivy.debug", false)
            IToast.debug = debug
            if (debug) {
                IToast.toast("debug mode! 非正式签名安装包")
            }
            ILog.logLevel = if (debug) 2 else 6
            appId = applicationInfo.metaData.getString("ivy.app.id", "")
            useNotch = applicationInfo.metaData.getBoolean("ivy.notch", false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkNotchSetting() {
        try {
            val applicationInfo = App.Instance.packageManager.getApplicationInfo(App.Instance.packageName, PackageManager.GET_META_DATA)
            if (applicationInfo.metaData.containsKey("ivy.notch")) {
                val notch = applicationInfo.metaData.getBoolean("ivy.notch", true)
                if (notch) {
                    ActivityUtil.Instance.activity?.window?.let { NotchCompat.immersiveDisplayCutout(it) } ?: ILog.e(
                        TAG, "set for use notch error! invalid activity"
                    )
                } else {
                    ActivityUtil.Instance.activity?.window?.let { NotchCompat.blockDisplayCutout(it) } ?: ILog.e(
                        TAG, "set for do not use notch error! invalid activity"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // =========================== 支付 ================================
    override fun isPaymentInitialized(): Boolean = ivyPay?.isPaymentInitialized() ?: run {
        ILog.i(TAG, "payment not setup yet!!!")
        false
    }

    override fun pay(id: Int, payload: String?, clientInfo: String?) {
        ivyPay?.pay(id, payload, clientInfo) ?: run {
            ILog.i(TAG, "pay::payment not setup yet!!!")
            builder?.purchaseResult?.payResult(id, IPurchaseResult.PAY_FAILED, payload, null)
        }
    }

    override fun shippingGoods(merchantTransactionId: String) {
        ivyPay?.shippingGoods(merchantTransactionId) ?: ILog.w(TAG, "pay::payment shipping goods failed,not init")
    }

    override fun getGoodsInfo(id: Int): String = ivyPay?.getGoodsInfo(id) ?: run {
        ILog.i(TAG, "getGoodsInfo::payment not setup yet!!! ")
        if (id == -1) "[]" else "{}"
    }

    override fun queryPurchase(id: Int) {
        ivyPay?.queryPurchase(id) ?: ILog.i(TAG, "queryPurchase::payment not setup yet!!!; $id")
    }

    private val purchaseResponseListener = object : IIPurchaseResult {

        override fun payResult(payId: Int, status: Int, payload: String?, merchantTransactionId: String?) {
            //支付结果
            ivyPay?.endPay()
            builder?.purchaseResult?.payResult(payId, status, payload, merchantTransactionId)
            if (status == IPurchaseResult.PAY_SUCCEED) {
                LocalStorage.Instance.encodeString(IKeys.KEY_PAYING_USER, "1")
                setUserProperty("paying_user", "1", TrackPlatform.THINkING_DATA)
            }
        }

        override fun onShippingResult(merchantTransactionId: String, status: Boolean) {
            builder?.purchaseResult?.onShippingResult(merchantTransactionId, status)
        }

        override fun onStoreInitialized(initState: Boolean) {
            builder?.purchaseResult?.onStoreInitialized(initState)
        }

        override fun logEvent(
            eventName: String,
            eventType: String,
            eventSrc: String,
            params: MutableMap<String, Any>?,
            platforms: List<TrackPlatform>?
        ) = this@IvySdk.logEvent(eventName, eventType, eventSrc, params, platforms)

    }
//=========================== 支付 ================================


    //=========================== grid data ================================
    override fun getGridConfig(key: String, defaultValue: String?): String? = GridManager.Instance.getGridConfig(key, defaultValue)

    override fun getGridConfig(key: String, defaultValue: Int): Int = GridManager.Instance.getGridConfig(key, defaultValue)

    override fun getGridConfig(key: String, defaultValue: Double): Double = GridManager.Instance.getGridConfig(key, defaultValue)

    override fun getGridConfig(key: String, defaultValue: Boolean): Boolean = GridManager.Instance.getGridConfig(key, defaultValue)

    override fun getGridConfig(key: String, defaultValue: Long): Long = GridManager.Instance.getGridConfig(key, defaultValue)
//=========================== grid data ================================

    //=========================== remote config ================================
    private fun setupFirebase() {
        try {
            if (firebaseImpl != null){
                return
            }
            firebaseImpl = Class.forName("com.ivy.sdk.firebase.FirebaseImpl").getDeclaredConstructor().newInstance() as IFirebase
        } catch (e: Exception) {
            ILog.e(TAG, "setup firebase failed:${e.message}")
        }
    }

    fun setupDefaultRemoteData(defaultData: String?) = firebaseImpl?.setupDefaultRemoteData(defaultData)
    fun getRemoteConfigString(key: String): String = firebaseImpl?.getRemoteConfigString(key) ?: ""

    fun getRemoteConfigDouble(key: String): Double = firebaseImpl?.getRemoteConfigDouble(key) ?: 0.0

    fun getRemoteConfigBoolean(key: String): Boolean = firebaseImpl?.getRemoteConfigBoolean(key) ?: false

    fun getRemoteConfigLong(key: String): Long = firebaseImpl?.getRemoteConfigLong(key) ?: 0L

    fun getIvyRemoteConfigInt(key: String): Int = IvyRemoteConfig.Instance.getInt(key)

    fun getIvyRemoteConfigString(key: String): String = IvyRemoteConfig.Instance.getString(key) ?: ""

    fun getIvyRemoteConfigDouble(key: String): Double = IvyRemoteConfig.Instance.getDouble(key)

    fun getIvyRemoteConfigBoolean(key: String): Boolean = IvyRemoteConfig.Instance.getBoolean(key)

    fun getIvyRemoteConfigLong(key: String): Long = IvyRemoteConfig.Instance.getLong(key)

//=========================== remote config ================================

    //======================== 客服 ===================================
    private val customerEvents = object : IHelperCallback {
        override fun onUnreadHelperMessageCount(count: Int) {
            builder?.helperListener?.onUnreadHelperMessageCount(count)
        }
    }

    override fun isHelperInitialized(): Boolean {
        return ivyHelper?.isHelperInitialized() ?: run {
            ILog.w(TAG, "customer had not initialized")
            false
        }
    }

    override fun hasNewHelperMessage(): Boolean {
        return ivyHelper?.hasNewHelperMessage() ?: run {
            ILog.w(TAG, "customer had not initialized")
            false
        }
    }

    override fun showHelper(entranceId: String, meta: String?, tags: String?, welcomeMessag: String?) {
        ivyHelper?.showHelper(entranceId, meta, tags, welcomeMessag) ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun showHelperSingleFAQ(faqId: String, moment: Int) {
        ivyHelper?.showHelperSingleFAQ(faqId, moment) ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun listenHelperUnreadMessageCount(onlyOnce: Boolean) {
        ivyHelper?.listenHelperUnreadMessageCount(onlyOnce) ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun stopListenHelperUnreadMessageCount() {
        ivyHelper?.stopListenHelperUnreadMessageCount() ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun updateHelperUserInfo(data: String?, tags: String?) {
        ivyHelper?.updateHelperUserInfo(data, tags) ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun resetHelperUserInfo() {
        ivyHelper?.resetHelperUserInfo() ?: ILog.w(TAG, "customer had not initialized")
    }

    override fun closeHelper() {
        ivyHelper?.closeHelper() ?: ILog.w(TAG, "customer had not initialized")
    }
//======================== 客服 ===================================

    // ======================== 游戏服务 ===================================
    private val authResult = object : IAuthResult {
        override fun onLoginResult(platform: String, status: Boolean, channel: String?, reason: String?) {
            builder?.authResult?.onLoginResult(platform, status, channel, reason)
            when (platform) {
                AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES -> {
                    setUserProperty("platform", AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, TrackPlatform.THINkING_DATA)
                    val id = ivyGames?.getPlayGamesUserId() ?: ""
                    setUserProperty("play_games_account_id", id, TrackPlatform.THINkING_DATA)
                }

                AuthPlatforms.LOGIN_PLATFORM_FACEBOOK -> {
                    setUserProperty("platform", AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, TrackPlatform.THINkING_DATA)
                    val id = ivyGames?.getFacebookUserId() ?: ""
                    setUserProperty("facebook_account_id", id, TrackPlatform.THINkING_DATA)
                }
            }
        }

        override fun onLogout(platform: String) {
            builder?.authResult?.onLogout(platform)
        }
    }

    fun hasPlayGamesSigned(): Boolean {
        return ivyGames?.isPlayGamesLogged() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            false
        }
    }

    fun loginPlayGames() {
        ivyGames?.loginPlayGames() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun logoutPlayGames() {
        ivyGames?.logoutPlayGames() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun getPlayGamesUserInfo(): String {
        return ivyGames?.getPlayGamesUserInfo() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            "{}"
        }
    }

    fun unlockAchievement(achievementId: String) {
        ivyGames?.unlockAchievement(achievementId) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun increaseAchievement(achievementId: String, step: Int) {
        ivyGames?.increaseAchievement(achievementId, step) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun showAchievement() {
        ivyGames?.showAchievement() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun showLeaderboards() {
        ivyGames?.showLeaderboards() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun showLeaderboard(leaderboardId: String) {
        ivyGames?.showLeaderboard(leaderboardId) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun updateLeaderboard(leaderboardId: String, score: Long) {
        ivyGames?.updateLeaderboard(leaderboardId, score) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun loginFacebook() {
        ivyGames?.loginFacebook() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun logoutFacebook() {
        ivyGames?.logoutFacebook() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun isFacebookLogged(): Boolean {
        return ivyGames?.isFacebookLogged() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            false
        }
    }

    fun getFacebookFriends(): String {
        return ivyGames?.getFacebookFriends() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            "[]"
        }
    }

    fun getFacebookUserInfo(): String {
        return ivyGames?.getFacebookUserInfo() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            "{}"
        }
    }

    fun logoutFirebase() {
        ivyGames?.logoutFirebase() ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun getFirebaseUserInfo(channel: String? = null): String {
        return ivyGames?.getFirebaseUserInfo(channel) ?: run {
            ILog.i(TAG, "ivy games unable to use")
            "{}"
        }
    }

    fun getFirebaseUserId(): String {
        return ivyGames?.getFirebaseUserId() ?: ""
    }

    fun isFirebaseAnonymousLogged(): Boolean {
        return ivyGames?.isFirebaseAnonymousLogged() ?: run {
            ILog.i(TAG, "ivy games unable to use")
            true
        }
    }

    fun isFirebaseLinkedWithChannel(channel: String): Boolean {
        return ivyGames?.isFirebaseLinkedWithChannel(channel) ?: run {
            ILog.i(TAG, "ivy games unable to use")
            false
        }
    }

    fun canFirebaseUnlinkWithChannel(channel: String): Boolean {
        return ivyGames?.canFirebaseUnlinkWithChannel(channel) ?: run {
            ILog.i(TAG, "ivy games unable to use")
            false
        }
    }

    fun unlinkFirebaseWithChannel(channel: String, callback: IFirebaseUnlink?) {
        ivyGames?.unlinkFirebaseWithChannel(channel, callback) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun reloadFirebaseLastSign(authReload: IFirebaseAuthReload) {
        ivyGames?.reloadFirebaseLastSign(authReload) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun loginAnonymous(authResult: IAuthResponse) {
        ivyGames?.loginAnonymous(authResult) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun loginWithPlayGames(authResult: IAuthResponse) {
        ivyGames?.loginWithPlayGames(authResult) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun loginWithFacebook(authResult: IAuthResponse) {
        ivyGames?.loginWithFacebook(authResult) ?: ILog.i(TAG, "ivy games unable to use")
    }

    fun loginWithEmailAndPassword(email: String, password: String, authResult: IAuthResponse) {
        ivyGames?.loginWithEmailAndPassword(email, password, authResult) ?: ILog.i(TAG, "ivy games unable to use")
    }

    //存档
    fun setArchive(collection: String, jsonData: String, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.set(userId, collection, jsonData, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun readArchive(collection: String, documentId: String? = null, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.read(userId, collection, documentId, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun mergeArchive(collection: String, jsonData: String, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.merge(userId, collection, jsonData, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun queryArchive(collection: String, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.query(userId, collection, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun deleteArchive(collection: String, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.delete(userId, collection, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun updateArchive(collection: String, jsonData: String, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.update(userId, collection, jsonData, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    fun snapshotArchive(collection: String, documentId: String?, callback: IArchiveResult) {
        ivyGames?.getFirebaseUserId()?.let { userId ->
            ivyGames?.snapshot(userId, collection, documentId, callback)
        } ?: {
            ILog.w(TAG, "invalid sign! check your firebase sign status")
            callback.onFailure(collection, null, "invalid sign")
        }
    }

    //存档

    //af邀请
    fun appsflyerInviteUser(channel: String, campaign: String, inviterId: String, inviterAppId: String) =
        IvyTrack.Instance.appsflyerInviteUser(channel, campaign, inviterId, inviterAppId)

    fun getAppsflyerInviterId(): String? = IvyTrack.Instance.getAppsflyerInviterId()
    //af邀请

//======================== 游戏服务 ===================================

    fun sendEmail(email: String, title: String?, extra: String?) {
        try {
            ActivityUtil.Instance.activity?.let { activity ->
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:$email"))
                title?.let { intent.putExtra(Intent.EXTRA_SUBJECT, it) }
                extra?.let { intent.putExtra(Intent.EXTRA_TEXT, it) }
                activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } ?: ILog.e(TAG, "send email failed:invalid activity")
        } catch (e: Exception) {
            ILog.e(TAG, "send email failed:${e.message}")
        }
    }

    private fun appendEventSrc(eventSrc: String, params: Map<String, Any>?): Map<String, Any> {
        val map = (params?.let { it.toMap() } ?: mutableMapOf<String, Any>()).toMutableMap()
        map["event_src"] = eventSrc
        return map
    }

    fun systemShareText(txt: String?) {
        if (txt.isNullOrEmpty()) {
            ILog.e(TAG, "share msg can not be null")
            return
        }
        ActivityUtil.Instance.activity?.let {
            IvyUtil.systemShareText(it, txt)
        }
    }

    fun systemShareImage(title: String?, imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            ILog.e(TAG, "share image path can not be null")
            return
        }
        var shareTitle: String = "Share"
        if (!title.isNullOrEmpty()) {
            shareTitle = title
        }
        ActivityUtil.Instance.activity?.let {
            IvyUtil.systemShareImage(shareTitle, imagePath)
        }
    }

    fun openUrl(url: String?) {
        IvyUtil.openUrlWithBrowser(url)
    }

    fun hasNotch(): Boolean = ActivityUtil.Instance.activity?.window?.let { NotchCompat.hasDisplayCutout(it) } ?: false

    fun getNotchHeight(): Int = ActivityUtil.Instance.activity?.window?.let {
        val data = NotchCompat.getDisplayCutoutSize(it)
        if (data.isEmpty()) {
            return@let 0
        } else {
            return@let data[0].height()
        }
    } ?: 0

    fun displayInNotch(activity: Activity) {
        try {
            activity.window.decorView.post {
                val hasNotch = NotchCompat.hasDisplayCutout(activity.window)
                if (hasNotch) {
                    if (useNotch) {
                        NotchCompat.immersiveDisplayCutout(activity.window)
                    } else {
                        NotchCompat.blockDisplayCutout(activity.window)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "set display notch failed:${e.message}")
        }
    }

    fun rateUs() {
        AppStoreUtil.rateUs(5, debug)
    }

    fun openAppStore(url: String?, referrer: String?) {
        AppStoreUtil.openAppStore(url, referrer)
    }

    fun copyTxt(txt: String?): Boolean {
        return txt?.let {
            val result = Util.copyTxt(it)
            ILog.i(TAG, "copy txt result:$result")
            return@let result
        } ?: run {
            ILog.i(TAG, "copy txt failed")
            false
        }
    }





}