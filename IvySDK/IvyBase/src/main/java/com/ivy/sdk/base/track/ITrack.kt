package com.ivy.sdk.base.track

import android.content.Context



enum class TrackPlatform(val value: String) {
    NONE("none"),
    FACEBOOK("facebook"),
    FIREBASE("firebase"),
    APPSFLYER("appsflyer"),
    THINkING_DATA("thinkingData");

}

interface IAppsflyerInvite {

    fun appsflyerInviteUser(
        channel: String, campaign: String, inviterId: String, inviterAppId: String
    )

    fun getAppsflyerInviterId(): String?

    fun getAppsflyerId():String?
}

interface IConversationCallback {
    fun onConversionDataSuccess(var1: Map<String, Any>?)

    fun onConversionDataFail(var1: String?)

    fun onAppOpenAttribution(var1: Map<String, String>?)

    fun onAttributionFailure(var1: String?)

}

abstract class ITrack : IAppsflyerInvite, IEvent {

    abstract fun setup(context: Context, appId:String, config: String, roleId:String, debug: Boolean, conversationCallback: IConversationCallback? = null)

    abstract fun setUserProperty(key: String, value: String)

}

abstract class AbsTrack : ITrack() {

    protected var enableStatus = false
    protected var enableAdPing = false
    protected var enablePurchasePing = false
    protected lateinit var context: Context

    protected var roleId: String? = null

    override fun setup(
        context: Context,
        appId:String,
        config: String,
        roleId:String,
        debug: Boolean,
        conversationCallback: IConversationCallback?
    ) {
        this.context = context
        this.roleId = roleId
    }

    override fun appsflyerInviteUser(
        channel: String,
        campaign: String,
        inviterId: String,
        inviterAppId: String
    ) {

    }

    override fun getAppsflyerInviterId(): String? = null

    override fun getAppsflyerId(): String? = null
}