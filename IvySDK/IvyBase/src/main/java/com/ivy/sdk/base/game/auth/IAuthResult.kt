package com.ivy.sdk.base.game.auth

interface IAuthResponse {
    fun onLoginResult(platform: String, status: Boolean, channel: String? = null, reason: String? = null)
}

interface IAuthResult : IAuthResponse {

    fun onLogout(platform: String)

}

object FirebaseLinkChannel {
        const val ANONYMOUS = "anonymous"
        const val PLAY_GAMES = "playgames"
        const val FACEBOOK = "facebook"
        const val EMAIL = "email"
}