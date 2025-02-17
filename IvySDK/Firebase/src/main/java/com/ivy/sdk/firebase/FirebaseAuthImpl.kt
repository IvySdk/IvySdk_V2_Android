package com.ivy.sdk.firebase


import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.PlayGamesAuthProvider
import com.ivy.sdk.base.game.auth.AuthPlatforms
import com.ivy.sdk.base.game.auth.FirebaseLinkChannel
import com.ivy.sdk.base.game.auth.IAuthResponse
import com.ivy.sdk.base.game.auth.IFirebaseAuth
import com.ivy.sdk.base.game.auth.IFirebaseAuthReload
import com.ivy.sdk.base.game.auth.IFirebaseUnlink
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

open class FirebaseAuthImpl : IFirebaseAuth {

    companion object {
        const val TAG = "Firebase"
    }

    private var debug: Boolean = false

    private var getPlayGamesAuthCode: (() -> String?)? = null
    private var getFacebookAccessToken: (() -> String?)? = null

    override fun setup(
        jsonObject: JSONObject, debug: Boolean, getPlayGamesAuthCode: (() -> String?), getFacebookAccessToken: (() -> String?)?
    ) {
        this.debug = debug
        this.getPlayGamesAuthCode = getPlayGamesAuthCode
        this.getFacebookAccessToken = getFacebookAccessToken
    }

    override fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * 检测上一次的登录状态
     * 1. 存在登录历史，尝试重新登录
     * 2. 不存在登录历史，返回失败
     */
    override fun reloadLastLoginStatus(authReload: IFirebaseAuthReload) {
        ILog.i(TAG, "reload last sign status")
        val currentUser = FirebaseAuth.getInstance().currentUser
        when (currentUser == null) {
            true -> {
                ILog.i(TAG, "current sign user is null, no sign history")
                authReload.onReload(false, "current user invalid")
            }

            false -> {
                ILog.i(TAG, "current user valid! start reload")
                currentUser.reload().addOnCompleteListener { task ->
                    when (task.isSuccessful) {
                        true -> {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user == null) {
                                ILog.i(TAG, "reload task succeed! but user invalid")
                                authReload.onReload(false, "invalid user")
                            } else {
                                ILog.i(TAG, "reload task succeed!")
                                authReload.onReload(true)
                            }
                        }

                        false -> {
                            ILog.i(TAG, "reload failed:${task.exception?.message}")
                            authReload.onReload(false, "${task.exception?.message}")
                        }
                    }
                }
            }
        }
    }

    override fun getUserInfo(channel: String?): String {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            return channel2ProviderId(channel)?.let { providerId ->
                val providerData = user.providerData
                for (provider in providerData) {
                    if (provider.providerId == providerId) {
                        try {
                            val json = JSONObject()
                            json.put("id", provider.uid)
                            json.put("name", provider.displayName ?: "")
                            json.put("photo", provider.photoUrl?.toString() ?: "")
                            json.put("email", provider.email ?: "")
                            return json.toString()
                        } catch (e: Exception) {
                            ILog.e(TAG, "get current user info err:${e.message}")
                        }
                    }
                }
                "{}"
            } ?: run {
                try {
                    val json = JSONObject()
                    json.put("id", user.uid)
                    json.put("name", user.displayName ?: "")
                    json.put("photo", user.photoUrl?.toString() ?: "")
                    json.put("email", user.email ?: "")
                    return@run json.toString()
                } catch (e: Exception) {
                    ILog.e(TAG, "get current user info err:${e.message}")
                }
                "{}"
            }

        } else {
            return "{}"
        }
    }

    override fun getUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    /**
     *  默认匿名登录状态
     */
    override fun isAnonymous(): Boolean = FirebaseAuth.getInstance().currentUser?.isAnonymous ?: true

    override fun isChannelLinked(channel: String): Boolean {
        if (channel.isEmpty()) return false
        val providerId = channel2ProviderId(channel)
        return FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            val infos = currentUser.providerData
            for (info in infos) {
                if (info != null && providerId == info.providerId) {
                    return@let true
                }
            }
            return@let false
        } ?: false
    }

    /**
     * play games 不可以解绑
     * 仅有一个link平台时不可解绑
     */
    override fun canChannelUnlink(channel: String): Boolean {
        if (channel.isEmpty()) return false
        val providerId = channel2ProviderId(channel)
        // play games 不可以解绑
        if (PlayGamesAuthProvider.PROVIDER_ID == providerId) return false
        return FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            val infos = currentUser.providerData
            var contains: Boolean = false
            var countLinks = 0
            for (info in infos) {
                if (info != null) {
                    if (providerId == info.providerId) {
                        contains = true
                    }
                    if (PlayGamesAuthProvider.PROVIDER_ID == info.providerId || FacebookAuthProvider.PROVIDER_ID == info.providerId || EmailAuthProvider.PROVIDER_ID == info.providerId) {
                        countLinks++
                    }
                }
            }
            return@let contains && countLinks >= 2
        } ?: false
    }

    override fun unlinkChannel(channel: String, callback: IFirebaseUnlink?) {
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            channel2ProviderId(channel)?.let { providerId ->
                currentUser.unlink(providerId).addOnCompleteListener { task ->
                    when (task.isSuccessful) {
                        true -> {
                            ILog.i(TAG, "unlink $channel success")
                            callback?.onUnlinked(channel, true)
                        }

                        false -> {
                            ILog.i(TAG, "unlink $channel failed:${task.exception?.message}")
                            callback?.onUnlinked(channel, false, "${task.exception?.message}")
                        }
                    }
                }
            } ?: callback?.onUnlinked(channel, false, "unable to unlink")
        } ?: callback?.onUnlinked(channel, false, "had not sign in")
    }

    override fun loginAnonymous(authResult: IAuthResponse) {
        try {
            ILog.i(TAG, "start sign Anonymous")
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            ILog.i(TAG, "sign Anonymous succeed! but user invalid")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.ANONYMOUS, "invalid user")
                        } else {
                            ILog.i(TAG, "sign Anonymous succeed")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.ANONYMOUS)
                        }
                    }

                    false -> {
                        ILog.i(TAG, "sign Anonymous failed:${task.exception?.message}")
                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.ANONYMOUS, "${task.exception?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            ILog.i(TAG, "sign Anonymous failed:${e.message}")
            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.ANONYMOUS, "${e.message}")
        }
    }

    /**
     * 主动登陆到play games
     */
    override fun loginWithPlayGames(authResult: IAuthResponse, reauth: Boolean) {
//        var currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null && currentUser.isAnonymous) {
//            ILog.i(TAG, "sign out current anonymous user")
//            FirebaseAuth.getInstance().signOut()
//        }

        val serverAuthCode = getPlayGamesAuthCode?.invoke()
        if (serverAuthCode.isNullOrEmpty()) {
            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, "invalid play games serverAuthCode")
            return
        }
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            ILog.i(TAG, "current user valid! check had linked with play-games yet")
            val providerData = currentUser.providerData
            for (info in providerData) {
                if (info != null && info.providerId == PlayGamesAuthProvider.PROVIDER_ID) {
                    ILog.i(TAG, "had linked with play-games !!! just re-auth")
                    val credential = PlayGamesAuthProvider.getCredential(serverAuthCode)
                    currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                        when (task.isSuccessful) {
                            true -> {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    ILog.i(TAG, "re-auth with play-games succeed! but user invalid")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.PLAY_GAMES, "invalid user")
                                } else {
                                    ILog.i(TAG, "re-auth with play-games succeed!")
                                    ILog.i(TAG, "re-auth with play-games succeed! but user invalid")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.PLAY_GAMES)
                                }
                            }

                            false -> {
                                ILog.i(TAG, "re-auth with play-games failed;${task.exception?.message}")
                                authResult.onLoginResult(
                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                    false,
                                    FirebaseLinkChannel.PLAY_GAMES,
                                    "${task.exception?.message}"
                                )
                            }
                        }
                    }
                    return
                }
            }

            ILog.i(TAG, "start link to play-games")
            val credential = PlayGamesAuthProvider.getCredential(serverAuthCode)
            currentUser.linkWithCredential(credential).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            ILog.i(TAG, "sign succeed with play-games! but user invalid")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.PLAY_GAMES, "invalid user")
                        } else {
                            ILog.i(TAG, "success link to play-games")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.PLAY_GAMES)
                        }
                    }

                    false -> {
                        ILog.i(TAG, "link to play-games failed;${task.exception?.message}")
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithPlayGames(authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.PLAY_GAMES,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(
                                            AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                            false,
                                            FirebaseLinkChannel.PLAY_GAMES,
                                            exception.message
                                        )
                                    }
                                }

                                else -> {
                                    ILog.i(TAG, "failed link to play-games; ${exception.message}")
                                    authResult.onLoginResult(
                                        AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                        false,
                                        FirebaseLinkChannel.PLAY_GAMES,
                                        "${exception.message}"
                                    )
                                }
                            }
                        } ?: {
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.PLAY_GAMES, "unknown")
                        }
                    }
                }
            }
        } ?: {
            ILog.i(TAG, "current user invalid! start sign with play-games")
            FirebaseAuth.getInstance().signInWithCredential(PlayGamesAuthProvider.getCredential(serverAuthCode)).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            ILog.i(TAG, "sign succeed with play-games! but user invalid")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.PLAY_GAMES, "invalid user")
                        } else {
                            ILog.i(TAG, "sign success with play-games")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.PLAY_GAMES)
                        }
                    }

                    false -> {
                        ILog.i(TAG, "sign with play games failed;${task.exception?.message}")
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithPlayGames(authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.PLAY_GAMES,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(
                                            AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                            false,
                                            FirebaseLinkChannel.PLAY_GAMES,
                                            exception.message
                                        )
                                    }
                                }

                                else -> {
                                    ILog.i(TAG, "failed link to play-games; ${exception.message}")
                                    authResult.onLoginResult(
                                        AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                        false,
                                        FirebaseLinkChannel.PLAY_GAMES,
                                        "${exception.message}"
                                    )
                                }
                            }
                        } ?: {
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.PLAY_GAMES, "unknown")
                        }
                    }
                }
            }
        }
    }

    override fun loginWithFacebook(authResult: IAuthResponse, reauth: Boolean) {
//        var currentUser = FirebaseAuth.getInstance().currentUser
//        if (currentUser != null && currentUser.isAnonymous) {
//            ILog.i(TAG, "sign out current anonymous user")
//            //FirebaseAuth.getInstance().signOut()
//        }
        val accessToken = getFacebookAccessToken?.invoke()
        if (accessToken.isNullOrEmpty()) {
            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, "invalid facebook access token")
            return
        }
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            ILog.i(TAG, "current user valid! check had linked with facebook yet")
            val providerData = currentUser.providerData
            for (info in providerData) {
                if (info != null && info.providerId == FacebookAuthProvider.PROVIDER_ID) {
                    ILog.i(TAG, "had linked with facebook!!! just re-auth")
                    val credential = FacebookAuthProvider.getCredential(accessToken)
                    currentUser.reauthenticate(credential).addOnCompleteListener { task ->
                        when (task.isSuccessful) {
                            true -> {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    ILog.i(TAG, "re-auth with facebook succeed! but user invalid")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, "invalid user")
                                } else {
                                    ILog.i(TAG, "re-auth with facebook succeed!")
                                    ILog.i(TAG, "re-auth with facebook succeed! but user invalid")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.FACEBOOK)
                                }
                            }

                            false -> {
                                ILog.i(TAG, "re-auth with facebook failed;${task.exception?.message}")
                                authResult.onLoginResult(
                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                    false,
                                    FirebaseLinkChannel.FACEBOOK,
                                    "${task.exception?.message}"
                                )
                            }
                        }
                    }
                    return
                }
            }

            ILog.i(TAG, "start link to facebook")
            val credential = FacebookAuthProvider.getCredential(accessToken)
            currentUser.linkWithCredential(credential).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            ILog.i(TAG, "sign succeed with facebook! but user invalid")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, "invalid user")
                        } else {
                            ILog.i(TAG, "success link to facebook")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.FACEBOOK)
                        }
                    }

                    false -> {
                        ILog.i(TAG, "link to facebook failed;${task.exception?.message}")
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithFacebook(authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.FACEBOOK,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, exception.message)
                                    }
                                }

                                else -> {
                                    ILog.i(TAG, "failed link to facebooks; ${exception.message}")
                                    authResult.onLoginResult(
                                        AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                        false,
                                        FirebaseLinkChannel.FACEBOOK,
                                        "${exception.message}"
                                    )
                                }
                            }
                        } ?: {
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, "unknown")
                        }
                    }
                }
            }
        } ?: {
            ILog.i(TAG, "current user invalid! start sign with facebook")
            FirebaseAuth.getInstance().signInWithCredential(FacebookAuthProvider.getCredential(accessToken)).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            ILog.i(TAG, "sign succeed with facebook! but user invalid")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, "invalid user")
                        } else {
                            ILog.i(TAG, "sign success with facebook")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.FACEBOOK)
                        }
                    }

                    false -> {
                        ILog.i(TAG, "sign with facebook failed;${task.exception?.message}")
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithFacebook(authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.FACEBOOK,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, exception.message)
                                    }
                                }

                                else -> {
                                    ILog.i(TAG, "failed link to facebooks; ${exception.message}")
                                    authResult.onLoginResult(
                                        AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                        false,
                                        FirebaseLinkChannel.FACEBOOK,
                                        "${exception.message}"
                                    )
                                }
                            }
                        } ?: {
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.FACEBOOK, "unknown")
                        }
                    }
                }
            }
        }
    }

    override fun loginWithEmailAndPassword(email: String, password: String, authResult: IAuthResponse, reauth: Boolean) {
        if (email.isEmpty() or password.isEmpty()) {
            ILog.i("sign with email failed:invalid email or password")
            authResult.onLoginResult(
                AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "invalid email or password"
            )
            return
        }
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            ILog.i(TAG, "already signed user; just link email")
            currentUser.linkWithCredential(EmailAuthProvider.getCredential(email, password)).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        ILog.i(TAG, "link to email success")
                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.EMAIL)
                        LocalStorage.Instance.encodeString("__saved_email", email)
                        LocalStorage.Instance.encodeString("__saved_password", password)
                    }

                    false -> {
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithEmailAndPassword(email, password, authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.EMAIL,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, exception.message)
                                    }
                                }

                                is FirebaseAuthInvalidUserException -> {
                                    ILog.i(TAG, "invalid user! start create")
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                        when (task.isSuccessful) {
                                            true -> {
                                                ILog.i(TAG, "create user success, start sign")
                                                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                                                    email, password
                                                ).addOnCompleteListener { signTask ->
                                                    when (signTask.isSuccessful) {
                                                        true -> {
                                                            ILog.i(TAG, "sign success")
                                                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.EMAIL)
                                                            LocalStorage.Instance.encodeString("__saved_email", email)
                                                            LocalStorage.Instance.encodeString("__saved_password", password)
                                                        }

                                                        false -> {
                                                            ILog.i(TAG, "sign failed!${signTask.exception?.message}")
                                                            authResult.onLoginResult(
                                                                AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                                false,
                                                                FirebaseLinkChannel.EMAIL,
                                                                "${signTask.exception?.message}"
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            false -> {
                                                ILog.i(TAG, "create user failed")
                                                authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "${task.exception?.message}"
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    ILog.i("sign with email failed:${exception.message}")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "${exception.message}")
                                }
                            }
                        } ?: {
                            ILog.i("sign with email failed with null exception")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "unknown")
                        }
                    }
                }
            }
        } ?: run {
            ILog.i(TAG, "no user signed! sign to email")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                when (task.isSuccessful) {
                    true -> {
                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.EMAIL)
                        LocalStorage.Instance.encodeString("__saved_email", email)
                        LocalStorage.Instance.encodeString("__saved_password", password)
                    }
                    false -> {
                        task.exception?.let { exception ->
                            when (exception) {
                                is FirebaseAuthRecentLoginRequiredException -> {
                                    ILog.i(TAG, "require recent login re-auth")
                                    if (reauth) {
                                        reauthentication { status ->
                                            ILog.i(TAG, "require recent login re-auth result:$status")
                                            when (status) {
                                                true -> loginWithEmailAndPassword(email, password, authResult, false)

                                                false -> authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                    false,
                                                    FirebaseLinkChannel.EMAIL,
                                                    exception.message
                                                )
                                            }
                                        }
                                    } else {
                                        ILog.i(TAG, "unable to reauthentication")
                                        authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, exception.message)
                                    }
                                }

                                is FirebaseAuthInvalidUserException -> {
                                    ILog.i(TAG, "invalid user! start create")
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                        when (task.isSuccessful) {
                                            true -> {
                                                ILog.i(TAG, "create user success, start sign")
                                                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                                                    email, password
                                                ).addOnCompleteListener { signTask ->
                                                    when (signTask.isSuccessful) {
                                                        true -> {
                                                            ILog.i(TAG, "sign success")
                                                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, true, FirebaseLinkChannel.EMAIL)
                                                            LocalStorage.Instance.encodeString("__saved_email", email)
                                                            LocalStorage.Instance.encodeString("__saved_password", password)
                                                        }

                                                        false -> {
                                                            ILog.i(TAG, "sign failed!${signTask.exception?.message}")
                                                            authResult.onLoginResult(
                                                                AuthPlatforms.LOGIN_PLATFORM_FIREBASE,
                                                                false,
                                                                FirebaseLinkChannel.EMAIL,
                                                                "${signTask.exception?.message}"
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            false -> {
                                                ILog.i(TAG, "create user failed")
                                                authResult.onLoginResult(
                                                    AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "${task.exception?.message}"
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    ILog.i("sign with email failed:${exception.message}")
                                    authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "${exception.message}")
                                }
                            }
                        } ?: {
                            ILog.i("sign with email failed with null exception")
                            authResult.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FIREBASE, false, FirebaseLinkChannel.EMAIL, "unknown")
                        }
                    }
                }
            }
        }
    }

    private fun reauthentication(onResult: (result: Boolean) -> Unit) {
        FirebaseAuth.getInstance().currentUser?.let { currentUser ->
            val providerIds: MutableSet<String> = mutableSetOf()
            currentUser.providerData.forEach {
                if (it != null) providerIds.add(it.providerId)
            }
            if (providerIds.contains(PlayGamesAuthProvider.PROVIDER_ID)) {
                ILog.i(TAG, "start reauthentication with play games")
                getPlayGamesAuthCode?.invoke()?.let { authCode ->
                    currentUser.reauthenticate(PlayGamesAuthProvider.getCredential(authCode)).addOnCompleteListener { task ->
                        ILog.i(TAG, "reauthentication response:${task.isSuccessful}; ${task.exception?.message}")
                        onResult.invoke(task.isSuccessful)
                    }
                } ?: {
                    ILog.i(TAG, "reauthentication failed; unable get play games server-auth-code")
                    onResult.invoke(false)
                }
            } else if (providerIds.contains(FacebookAuthProvider.PROVIDER_ID)) {
                ILog.i(TAG, "start reauthentication with facebook")
                getFacebookAccessToken?.invoke()?.let { accesstoken ->
                    currentUser.reauthenticate(FacebookAuthProvider.getCredential(accesstoken)).addOnCompleteListener { task ->
                        ILog.i(TAG, "reauthentication response:${task.isSuccessful}; ${task.exception?.message}")
                        onResult.invoke(task.isSuccessful)
                    }
                } ?: {
                    ILog.i(TAG, "reauthentication failed; unable get facebook accessToken")
                    onResult.invoke(false)
                }
            } else if (providerIds.contains(EmailAuthProvider.PROVIDER_ID)) {
                ILog.i(TAG, "start reauthentication with email")
                val email = LocalStorage.Instance.decodeString("__saved_email")
                val password = LocalStorage.Instance.decodeString("__saved_password")
                if (email.isNullOrEmpty() or password.isNullOrEmpty()) {
                    ILog.i(TAG, "reauthentication failed; invalid email:${email} or password:${password}")
                    onResult.invoke(false)
                } else {
                    currentUser.reauthenticate(EmailAuthProvider.getCredential(email!!, password!!)).addOnCompleteListener { task ->
                        ILog.i(TAG, "reauthentication response:${task.isSuccessful}; ${task.exception?.message}")
                        onResult.invoke(task.isSuccessful)
                    }
                }
            } else {
                ILog.i(TAG, "reauthentication failed; not valid provider id")
                onResult.invoke(false)
            }
        } ?: {
            ILog.i(TAG, "reauthentication failed; no signed user")
            onResult.invoke(false)
        }
    }

    private fun channel2ProviderId(channel: String?): String? {
        return when (channel) {
            FirebaseLinkChannel.PLAY_GAMES -> PlayGamesAuthProvider.PROVIDER_ID
            FirebaseLinkChannel.FACEBOOK -> FacebookAuthProvider.PROVIDER_ID
            FirebaseLinkChannel.EMAIL -> EmailAuthProvider.PROVIDER_ID
            else -> null
        }
    }

}