package com.ivy.sdk.base.game.auth

import android.content.Intent
import com.ivy.sdk.base.game.archive.IArchive

interface IGame : IArchive {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun preCheckLastLoginStatus()

    fun isPlayGamesLogged(): Boolean

    fun loginPlayGames()

    fun logoutPlayGames()

    fun getPlayGamesUserInfo(): String

    fun getPlayGamesUserId(): String

    fun unlockAchievement(achievementId: String)

    fun increaseAchievement(achievementId: String, step: Int)

    fun showAchievement()

    fun showLeaderboards()

    fun showLeaderboard(leaderboardId: String)

    fun updateLeaderboard(leaderboardId: String, score: Long)

    fun loginFacebook()

    fun logoutFacebook()

    fun isFacebookLogged(): Boolean

    fun getFacebookFriends(): String

    fun getFacebookUserInfo(): String

    fun getFacebookUserId(): String

    fun logoutFirebase()

    fun getFirebaseUserInfo(channel: String? = null): String

    fun getFirebaseUserId(): String

    fun isFirebaseAnonymousLogged(): Boolean

    fun isFirebaseLinkedWithChannel(channel: String): Boolean

    fun canFirebaseUnlinkWithChannel(channel: String): Boolean

    fun unlinkFirebaseWithChannel(channel: String, callback: IFirebaseUnlink?)

    fun reloadFirebaseLastSign(authReload: IFirebaseAuthReload)

    fun loginAnonymous(authResult: IAuthResponse)

    fun loginWithPlayGames(authResult: IAuthResponse)

    fun loginWithFacebook(authResult: IAuthResponse)

    fun loginWithEmailAndPassword(email: String, password: String, authResult: IAuthResponse)


}