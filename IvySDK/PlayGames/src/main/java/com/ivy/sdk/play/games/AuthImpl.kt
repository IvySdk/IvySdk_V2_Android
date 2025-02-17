package com.ivy.sdk.play.games

import android.content.Intent
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.PlayGamesSdk
import com.google.android.gms.games.Player
import com.ivy.sdk.base.App
import com.ivy.sdk.base.game.auth.AuthPlatforms
import com.ivy.sdk.base.game.auth.IAuthResponse
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.game.auth.IPlayGameAuth
import com.ivy.sdk.base.net.HttpUtil
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

open class AuthImpl : IPlayGameAuth {

    companion object {
        const val TAG = "PlayGames"
    }

    private var appId: String = ""
    private var web_client_id: String? = null
    private var authResult: IAuthResult? = null
    private var isAuthenticated: Boolean = false
    private var userInfo: String = "{}"
    private var serverAuthCode: String = ""
    private var verifyUrl: String? = null

    override fun setup(appId: String, jsonObject: JSONObject, debug: Boolean, authResult: IAuthResult) {
        this.appId = appId
        this.authResult = authResult
        this.web_client_id = jsonObject.optString("web_client_id")
        if (web_client_id.isNullOrEmpty()) {
            ILog.e(TAG, "web_client_id can not be empty!!!")
            return
        }
        this.verifyUrl = jsonObject.optString("verify_url")
        PlayGamesSdk.initialize(App.Instance)
        loadLoginStatus()
    }

    override fun requireServerSideAuthCode(): String? = serverAuthCode

    override fun getPlayGamesUserInfo(): String = userInfo

    override fun unlockAchievement(achievementId: String) {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getAchievementsClient(activity).unlock(achievementId)
        } ?: ILog.w(TAG, "unable to unlock achievement! invalid activity instance")
    }

    override fun increaseAchievement(achievementId: String, step: Int) {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getAchievementsClient(activity).increment(achievementId, step)
        } ?: ILog.w(TAG, "unable to increase achievement! invalid activity instance")
    }

    override fun showAchievement() {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getAchievementsClient(activity).achievementsIntent.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        activity.startActivityForResult(task.result, 99992)
                    } catch (e: Exception) {
                        ILog.w(TAG, "show achievement failed when startActivityForResult;${e.message}")
                    }
                } else {
                    ILog.w(TAG, "show achievement failed; ${task.exception?.message}")
                }
            }
        } ?: ILog.w(TAG, "unable to show achievement! invalid activity instance")
    }

    override fun showLeaderboards() {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getLeaderboardsClient(activity).allLeaderboardsIntent.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        activity.startActivityForResult(task.result, 99991)
                    } catch (e: Exception) {
                        ILog.w(TAG, "show leaderboard failed when startActivityForResult;${e.message}")
                    }
                } else {
                    ILog.w(TAG, "show leaderboard failed; ${task.exception?.message}")
                }
            }
        } ?: ILog.w(TAG, "unable to show leaderboards! invalid activity instance")
    }

    override fun showLeaderboard(leaderboardId: String) {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getLeaderboardsClient(activity).getLeaderboardIntent(leaderboardId)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        try {
                            activity.startActivityForResult(task.result, 99991)
                        } catch (e: Exception) {
                            ILog.w(TAG, "show leaderboard${leaderboardId} failed when startActivityForResult;${e.message}")
                        }
                    } else {
                        ILog.w(TAG, "show leaderboard${leaderboardId} failed; ${task.exception?.message}")
                    }
                }
        } ?: ILog.w(TAG, "unable to show leaderboard:${leaderboardId}! invalid activity instance")
    }

    override fun updateLeaderboard(leaderboardId: String, score: Long) {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getLeaderboardsClient(activity).submitScore(leaderboardId, score)
        } ?: ILog.w(TAG, "unable to update leaderboard:${leaderboardId}! invalid activity instance")
    }

    override fun autoLogin() {
        // play games 在启动时主动触发登录
    }

    override fun loadLoginStatus() {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getGamesSignInClient(activity).isAuthenticated.addOnCompleteListener { task ->
                isAuthenticated = task.isSuccessful && task.result.isAuthenticated
                ILog.i(TAG, "load sign status:$isAuthenticated")
                if (isAuthenticated) {
                    loadServerAuthCode(callback = { state, msg ->
                        if (state) {
                            authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, true)
                        } else {
                            authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, msg)
                        }
                    })
                } else {
                    authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false,  null,"${task.exception?.message}")
                }
            }
        } ?: {
            ILog.w(TAG, "unable to log sign status! invalid activity instance")
            authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null,"invalid activity instance")
        }
    }

    override fun login(authCallback: IAuthResponse?) {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getGamesSignInClient(activity).signIn()
                .addOnCompleteListener { task ->
                    when (task.isSuccessful) {
                        true -> {
                            ILog.i(TAG, "sign response success")
                            loadServerAuthCode(callback = { state, msg ->
                                isAuthenticated = state
                                if (state) {
                                    authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, true) ?: authResult?.onLoginResult(
                                        AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES,
                                        true
                                    )
                                } else {
                                    authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, msg)
                                        ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, msg)
                                }
                            })
                        }

                        false -> {
                            isAuthenticated = false
                            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null,"${task.exception?.message}")
                                ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, "${task.exception?.message}")
                            ILog.i(TAG, "sign failed:${task.exception?.message}")
                        }
                    }
                }
        } ?: {
            ILog.w(TAG, "unable to log sign status! invalid activity instance")
            isAuthenticated = false
            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, "invalid activity instance")
                ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_PLAY_GAMES, false, null, "invalid activity instance")
        }
    }

    override fun logout() {
        ILog.w(TAG, "play games can not sign out")
    }

    override fun getLoginStatus(): Boolean = isAuthenticated

    override fun getUserInfo(): String {
        if (userInfo == "{}") {
            CoroutineScope(Dispatchers.Main).launch { loadUserInfo() }
        }
        return userInfo
    }

    override fun getUserId(): String {
        try {
            return JSONObject(userInfo).getString("id")
        } catch (_: Exception) {
        }
        return ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    private fun loadUserInfo() {
        ActivityUtil.Instance.activity?.let { activity ->
            PlayGames.getPlayersClient(activity).currentPlayer.addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        try {
                            val player: Player = task.result
                            val json = JSONObject()
                            json.put("id", player.playerId)
                            json.put("name", player.displayName)
                            json.put("photo", player.iconImageUri?.toString() ?: "")
                            userInfo = json.toString()
                            ILog.i(TAG, "get player success:$userInfo")
                        } catch (e: Exception) {
                            ILog.e(TAG, "get player err:${e.message}")
                            userInfo = "{}"
                        }
                    }

                    false -> {
                        userInfo = "{}"
                        ILog.e(TAG, "get player err:${task.exception?.message}")
                    }
                }
            }
        } ?: ILog.i(TAG, "require server auth code failed:invalid activity instance")
    }

    private fun loadServerAuthCode(callback: (state: Boolean, msg: String?) -> Unit) {
        ActivityUtil.Instance.activity?.let { activity ->
            web_client_id?.let { oauth2WebClientId ->
                PlayGames.getGamesSignInClient(activity)
                    .requestServerSideAccess(oauth2WebClientId, false).addOnCompleteListener { task ->
                        when (task.isSuccessful) {
                            true -> {
                                serverAuthCode = task.result
                                ILog.i(TAG, "require server auth code success:$serverAuthCode")
                                verifyUrl?.let {
                                    val result = verifyLogin(it, serverAuthCode)
                                    if (result) {
                                        loadUserInfo()
                                        callback.invoke(true, null)
                                    } else {
                                        callback.invoke(false, "verify failed")
                                    }
                                } ?: run {
                                    loadUserInfo()
                                    callback.invoke(true, null)
                                }
                            }

                            false -> {
                                ILog.i(TAG, "require server auth code failed:${task.exception?.message}")
                                callback.invoke(false, "${task.exception?.message}")
                            }
                        }
                    }
            } ?: run {
                ILog.i(TAG, "require server auth code failed:invalid web client id")
                callback.invoke(false, "invalid web client id")
            }
        } ?: run {
            ILog.i(TAG, "require server auth code failed:invalid activity instance")
            callback.invoke(false, "invalid activity instance")
        }
    }

    private fun verifyLogin(url: String, token: String): Boolean {
        try {
            val json = JSONObject()
            json.put("is_encrypt", false)
            val data = JSONObject()
            data.put("app_id", appId)
            data.put("token", token)
            json.put("data", data)
            val okHttpClient = HttpUtil.Instance.okHttpClient
            val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
            val postBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(url).post(postBody).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val str = response.body?.string()
                val userId = JSONObject(str).getString("data")
                if (!userId.isNullOrEmpty()) {
                    return true
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "verify login failed:${e.message}")
        }
        return false
    }

}