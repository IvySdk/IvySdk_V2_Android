package com.ivy.sdk.base.game.auth

import org.json.JSONObject


interface IFirebaseUnlink {
    fun onUnlinked(unlinkChannel: String, status: Boolean, reason: String? = "")
}

interface IFirebaseAuthReload {
    fun onReload(status: Boolean, reason: String? = "")
}

interface IFirebaseAuth {

    fun setup(
        jsonObject: JSONObject,
        debug: Boolean,
        getPlayGamesAuthCode: (() -> String?),
        getFacebookAccessToken: (() -> String?)?
    )

//    fun signIn(
//        channel: String,
//        email: String?,
//        emailPassword: String?
//    )

    fun logout()

    fun reloadLastLoginStatus(authReload: IFirebaseAuthReload)

    fun getUserInfo(channel: String? = null): String

    fun getUserId(): String

    fun isAnonymous(): Boolean

    fun isChannelLinked(channel: String): Boolean

    fun canChannelUnlink(channel: String): Boolean

    fun unlinkChannel(channel: String, callback: IFirebaseUnlink?)

    fun loginAnonymous(authResult: IAuthResponse)

    fun loginWithPlayGames(authResult: IAuthResponse, reauth: Boolean = true)

    fun loginWithFacebook(authResult: IAuthResponse, reauth: Boolean = true)

    fun loginWithEmailAndPassword(email: String, password: String, authResult: IAuthResponse, reauth: Boolean = true)

}