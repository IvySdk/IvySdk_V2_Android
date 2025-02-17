package com.ivy.sdk.games

import android.content.Intent
import com.ivy.sdk.base.game.archive.IArchiveResult
import com.ivy.sdk.base.game.archive.IIArchive
import com.ivy.sdk.base.game.auth.AuthPlatforms
import com.ivy.sdk.base.game.auth.FirebaseLinkChannel
import com.ivy.sdk.base.game.auth.IAuthResponse
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.game.auth.IFacebookAuth
import com.ivy.sdk.base.game.auth.IFirebaseAuth
import com.ivy.sdk.base.game.auth.IFirebaseAuthReload
import com.ivy.sdk.base.game.auth.IFirebaseUnlink
import com.ivy.sdk.base.game.auth.IGame
import com.ivy.sdk.base.game.auth.IPlayGameAuth
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 *  play games 和 Google+ 登录修改;
 *  {@link https://developer.android.google.cn/games/pgs/android/migrate-to-v2?hl=zh-cn}
 *  1. 版本 play games在游戏启动时会主动触发登录
 *  1. V2 版本 play games 登录不再强制需要Google+ 账号登录
 *  3. V2 版本 play games 移除了退出登录方法
 *  所以 play games 和 Google+ 登录 不再是绑定状态
 */
open class IvyGames : IGame {

    companion object {
        const val TAG = "games"
    }

    private var facebookAuth: IFacebookAuth? = null
    private var playGamesAuth: IPlayGameAuth? = null
    private var firebaseAuth: IFirebaseAuth? = null

    private var archiveImpl: IIArchive? = null

    fun setup(appId:String, data: String, authResult: IAuthResult, debug: Boolean) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val json = JSONObject(data)
                if (json.has("playGames")) {
                    json.optJSONObject("playGames")?.let { config ->
                        launch(Dispatchers.Main) {
                            try {
                                playGamesAuth =
                                    Class.forName("com.ivy.sdk.play.games.AuthImpl").getDeclaredConstructor().newInstance() as? IPlayGameAuth
                                playGamesAuth?.setup(appId, config, debug, authResult)
                            } catch (e: Exception) {
                                ILog.e(TAG, "create play-game impl failed:${e.message}")
                            }
                        }
                    }
                }
                if (json.has("google+")) {
                    ILog.w(TAG, "unsupported g+ sign")
//                    json.optJSONObject("google+")?.let { config ->
//                        launch(Dispatchers.Main) {
//                            googleSignImpl =
//                                GoogleSignImpl().apply { this.setup(config, loginResult, debug) }
//                        }
//                    }
                }
                if (json.has("firebase")) {
                    json.optJSONObject("firebase")?.let { config ->
                        launch(Dispatchers.Main) {
                            try {
                                firebaseAuth =
                                    Class.forName("com.ivy.sdk.firebase.FirebaseAuthImpl").getDeclaredConstructor().newInstance() as? IFirebaseAuth
                                firebaseAuth?.setup(config, debug, getPlayGamesAuthCode = {
                                    playGamesAuth?.requireServerSideAuthCode()
                                }, getFacebookAccessToken = {
                                    facebookAuth?.requireAccessToken()
                                })
                            } catch (e: Exception) {
                                ILog.e(TAG, "create firebase impl failed:${e.message}")
                            }
                        }
                    }
                }
                if (json.has("facebook")) {
                    json.optJSONObject("facebook")?.let { config ->
                        launch(Dispatchers.Main) {
                            try {
                                facebookAuth =
                                    Class.forName("com.ivy.sdk.facebook.auth.AuthImpl").getDeclaredConstructor().newInstance() as? IFacebookAuth
                                facebookAuth?.setup(appId, config, debug, authResult)
                            } catch (e: Exception) {
                                ILog.e(TAG, "create facebook impl failed:${e.message}")
                            }
                        }
                    }
                }
                if (json.has("firestore")) {
                    json.optJSONObject("firestore")?.let { config ->
                        launch(Dispatchers.Main) {
                            try {
                                archiveImpl =
                                    Class.forName("com.ivy.sdk.firestore.FirestoreImpl").getDeclaredConstructor().newInstance() as? IIArchive
                                archiveImpl?.setup(config, debug)
                            } catch (e: Exception) {
                                ILog.e(TAG, "create firestore impl failed:${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                ILog.e(TAG, "parse games config err:${e.message}")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookAuth?.onActivityResult(requestCode, resultCode, data)
    }

    override fun preCheckLastLoginStatus() {
        playGamesAuth?.loadLoginStatus()
    }

    override fun isPlayGamesLogged(): Boolean {
        return playGamesAuth?.getLoginStatus() ?: run {
            ILog.w(TAG, "play-games auth invalid to use")
            false
        }
    }

    override fun loginPlayGames() {
        playGamesAuth?.login() ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun logoutPlayGames() {
        playGamesAuth?.logout() ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun getPlayGamesUserInfo(): String {
        return playGamesAuth?.getUserInfo() ?: run {
            ILog.w(TAG, "play-games auth invalid to use")
            "{}"
        }
    }

    override fun getPlayGamesUserId(): String {
        return playGamesAuth?.getUserId() ?: ""
    }

    override fun unlockAchievement(achievementId: String) {
        playGamesAuth?.unlockAchievement(achievementId) ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun increaseAchievement(achievementId: String, step: Int) {
        playGamesAuth?.increaseAchievement(achievementId, step) ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun showAchievement() {
        playGamesAuth?.showAchievement() ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun showLeaderboards() {
        playGamesAuth?.showLeaderboards() ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun showLeaderboard(leaderboardId: String) {
        playGamesAuth?.showLeaderboard(leaderboardId) ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun updateLeaderboard(leaderboardId: String, score: Long) {
        playGamesAuth?.updateLeaderboard(leaderboardId, score) ?: ILog.w(TAG, "play-games auth invalid to use")
    }

    override fun loginFacebook() {
        facebookAuth?.login() ?: ILog.w(TAG, "facebook auth invalid to use")
    }

    override fun logoutFacebook() {
        facebookAuth?.login() ?: ILog.w(TAG, "facebook auth invalid to use")
    }

    override fun isFacebookLogged(): Boolean {
        return facebookAuth?.getLoginStatus() ?: run {
            ILog.w(TAG, "facebook auth invalid to use")
            false
        }
    }

    override fun getFacebookFriends(): String {
        return facebookAuth?.getFriends() ?: run {
            ILog.w(TAG, "facebook auth invalid to use")
            "[]"
        }
    }

    override fun getFacebookUserInfo(): String {
        return facebookAuth?.getUserInfo() ?: run {
            ILog.w(TAG, "facebook auth invalid to use")
            "{}"
        }
    }

    override fun getFacebookUserId(): String {
        return facebookAuth?.getUserId() ?: ""
    }

    override fun logoutFirebase() {
        firebaseAuth?.logout() ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun getFirebaseUserInfo(channel: String?): String {
        return firebaseAuth?.getUserInfo(channel) ?: run {
            ILog.w(TAG, "firebase auth invalid to use")
            "{}"
        }
    }

    override fun getFirebaseUserId(): String {
        return firebaseAuth?.getUserId() ?: run {
            ILog.w(TAG, "firebase auth invalid to use")
            ""
        }
    }

    override fun isFirebaseAnonymousLogged(): Boolean {
        return firebaseAuth?.isAnonymous() ?: run {
            ILog.w(TAG, "firebase auth invalid to use")
            true
        }
    }

    override fun isFirebaseLinkedWithChannel(channel: String): Boolean {
        return firebaseAuth?.isChannelLinked(channel) ?: run {
            ILog.w(TAG, "firebase auth invalid to use")
            false
        }
    }

    override fun canFirebaseUnlinkWithChannel(channel: String): Boolean {
        return firebaseAuth?.canChannelUnlink(channel) ?: run {
            ILog.w(TAG, "firebase auth invalid to use")
            false
        }
    }

    override fun unlinkFirebaseWithChannel(channel: String, callback: IFirebaseUnlink?) {
        firebaseAuth?.unlinkChannel(channel, callback) ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun reloadFirebaseLastSign(authReload: IFirebaseAuthReload) {
        firebaseAuth?.reloadLastLoginStatus(authReload) ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun loginAnonymous(authResult: IAuthResponse) {
        firebaseAuth?.loginAnonymous(authResult) ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun loginWithPlayGames(authResult: IAuthResponse) {
        firebaseAuth?.let { impl ->
            when (isPlayGamesLogged()) {
                true -> {
                    ILog.i(TAG, "play games signed already,start sign to firebase")
                    impl.loginWithPlayGames(authResult)
                }

                false -> {
                    ILog.i(TAG, "play-games had not signed! try sign to play-games")
                    playGamesAuth?.login(object : IAuthResponse {
                        override fun onLoginResult(platform: String, status: Boolean, channel: String?, reason: String?) {
                            when (status) {
                                true -> {
                                    ILog.i(TAG, "play-games sign success! start sign to firebase")
                                    impl.loginWithPlayGames(authResult)
                                }

                                false -> {
                                    ILog.e(TAG, "play-games sign failed:$reason")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, channel, "play-games sign failed")
                                }
                            }
                        }
                    }) ?: {
                        ILog.w(TAG, "play-games unsupported to sign! check your config")
                        authResult.onLoginResult(
                            AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                            false,
                            FirebaseLinkChannel.PLAY_GAMES,
                            "unable to sign play-games"
                        )
                    }
                }
            }
        } ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun loginWithFacebook(authResult: IAuthResponse) {
        firebaseAuth?.let { impl ->
            when (isFacebookLogged()) {
                true -> {
                    ILog.i(TAG, "facebook signed already, start sign to firebase")
                    impl.loginWithFacebook(authResult)
                }

                false -> {
                    ILog.i(TAG, "facebook had not signed! try sign to play-games")
                    facebookAuth?.login(object : IAuthResponse {
                        override fun onLoginResult(platform: String, status: Boolean, channel: String?, reason: String?) {
                            when (status) {
                                true -> {
                                    ILog.i(TAG, "facebook sign success! start sign to firebase")
                                    impl.loginWithFacebook(authResult)
                                }

                                false -> {
                                    ILog.e(TAG, "facebook sign failed:$reason")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, channel, "facebook sign failed")
                                }
                            }
                        }
                    }) ?: {
                        ILog.w(TAG, "facebook unsupported to sign! check your config")
                        authResult.onLoginResult(
                            AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                            false,
                            FirebaseLinkChannel.FACEBOOK,
                            "unable to sign facebook"
                        )
                    }
                }
            }
        } ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun loginWithEmailAndPassword(email: String, password: String, authResult: IAuthResponse) {
        firebaseAuth?.loginWithEmailAndPassword(email, password, authResult) ?: ILog.w(TAG, "firebase auth invalid to use")
    }

    override fun set(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        archiveImpl?.set(userId, collection, jsonData, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun read(userId: String, collection: String, documentId: String?, callback: IArchiveResult) {
        archiveImpl?.read(userId, collection, documentId, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun merge(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        archiveImpl?.merge(userId, collection, jsonData, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun query(userId: String, collection: String, callback: IArchiveResult) {
        archiveImpl?.query(userId, collection, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun delete(userId: String, collection: String, callback: IArchiveResult) {
        archiveImpl?.delete(userId, collection, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun update(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        archiveImpl?.update(userId, collection, jsonData, callback) ?: ILog.w(TAG, "archive invalid to user")
    }

    override fun snapshot(userId: String, collection: String, documentId: String?, callback: IArchiveResult) {
        archiveImpl?.snapshot(userId, collection, documentId, callback) ?: ILog.w(TAG, "archive invalid to user")
    }


}