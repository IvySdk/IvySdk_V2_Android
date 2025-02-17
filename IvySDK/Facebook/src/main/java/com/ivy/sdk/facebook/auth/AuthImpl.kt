package com.ivy.sdk.facebook.auth

import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.LoginStatusCallback
import com.facebook.Profile
import com.facebook.internal.Utility
import com.facebook.internal.Utility.GraphMeRequestWithCacheCallback
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.ivy.sdk.base.App
import com.ivy.sdk.base.game.auth.AuthPlatforms
import com.ivy.sdk.base.game.auth.IAuthResponse
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.game.auth.IFacebookAuth
import com.ivy.sdk.base.utils.ActivityUtil

import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject


open class AuthImpl : IFacebookAuth {

    companion object {
        const val TAG = "Facebook"

        const val PERMISSION_PUBLIC_PROFILE = "public_profile"
        const val PERMISSION_EMAIL = "email"
        const val PERMISSION_USER_FRIENDS = "user_friends"
    }

    private var authResult: IAuthResult? = null
    private val permissions: MutableList<String> = mutableListOf()
    private var callbackManager: CallbackManager? = null

    private var friends: String = "[]"
    private var authCallback: IAuthResponse? = null

    override fun setup(appId: String, jsonObject: JSONObject, debug: Boolean, authResult: IAuthResult) {
        this.authResult = authResult
        callbackManager = CallbackManager.Factory.create()
        try {
            val permissionArr = jsonObject.getJSONArray("permissions")
            for (index in 0 until permissionArr.length()) {
                permissions.add(permissionArr.optString(index))
            }
            ILog.i(TAG, "setup permissions:${permissions.joinToString()}")
        } catch (e: Exception) {
            ILog.e(TAG, "format configured permission err:${e.message}; setup default permissions")
            permissions.add(PERMISSION_EMAIL)
            permissions.add(PERMISSION_PUBLIC_PROFILE)
        }
        autoLogin()
    }

    override fun getFriends(): String = friends

    override fun requireAccessToken(): String? = AccessToken.getCurrentAccessToken()?.token


    override fun autoLogin() {
        LoginManager.getInstance().retrieveLoginStatus(App.Instance, object : LoginStatusCallback {
            override fun onCompleted(accessToken: AccessToken) {
                ILog.w(TAG, "retrieve login status completed;accessToken:${accessToken}")
                AccessToken.setCurrentAccessToken(accessToken)
                if (getLoginStatus()) {
                    ILog.i(TAG, "retrieve login status:: log success,start load user info")
                    refreshAccessToken()
                    requireUserInfo()
                    if (permissions.contains(PERMISSION_USER_FRIENDS)) {
                        requireFriends()
                    }
                } else {
                    ILog.i(TAG, "retrieve login status:: not logged yet")
                }
            }

            override fun onError(exception: Exception) {
                ILog.e(TAG, "retrieve login status err:${exception.message}")
                AccessToken.setCurrentAccessToken(null)
            }

            override fun onFailure() {
                ILog.w(TAG, "retrieve login failure")
                AccessToken.setCurrentAccessToken(null)
            }
        })
    }

    override fun loadLoginStatus() {

    }

    override fun login(authCallback: IAuthResponse?) {
        ActivityUtil.Instance.activity?.let { activity ->
            this.authCallback = authCallback
            LoginManager.getInstance().registerCallback(callbackManager, loginCallback)
            LoginManager.getInstance().logIn(activity, permissions)
        } ?: {
            ILog.i(TAG, "sign facebook failed: invalid activity")
            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "invalid activity")
                ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "invalid activity")
        }
    }

    override fun logout() {
        LoginManager.getInstance().logOut()
        authResult?.onLogout(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK)
    }

    override fun getLoginStatus(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    override fun getUserInfo(): String {
        return checkProfile() ?: "{}"
    }

    override fun getUserId(): String {
        return Profile.getCurrentProfile()?.id ?: ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ILog.i(TAG, "received:onActivityResult::requestCode:$requestCode;resultCode:$resultCode")
        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private val loginCallback = object : FacebookCallback<LoginResult> {
        override fun onCancel() {
            ILog.i(TAG, "sign in canceled")
            AccessToken.setCurrentAccessToken(null)
            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "user canceled")
                ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "user canceled")
            //   LoginManager.getInstance().unregisterCallback(callbackManager)
            authCallback = null
        }

        override fun onError(error: FacebookException) {
            ILog.w(TAG, "sign in error:${error.message}")
            AccessToken.setCurrentAccessToken(null)
            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, error.message)
                ?: authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, error.message)
            //   LoginManager.getInstance().unregisterCallback(callbackManager)
            authCallback = null
        }

        override fun onSuccess(result: LoginResult) {
            ILog.i(TAG, "sign in success:")
            val grantedPermissions = result.recentlyGrantedPermissions
            ILog.i(TAG, "granted permissions:${grantedPermissions.joinToString()}")
            authCallback?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, true)
            authCallback = null
            requireUserInfo(responseClient = true)
            if (grantedPermissions.contains(PERMISSION_USER_FRIENDS)) {
                ILog.i(TAG, "friends permission allowed; start load friends list")
                requireFriends()
            }
            //LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    private fun refreshAccessToken() {
        AccessToken.refreshCurrentAccessTokenAsync(object : AccessToken.AccessTokenRefreshCallback {
            override fun OnTokenRefreshFailed(exception: FacebookException?) {
                ILog.i(TAG, "refresh access token failed:${exception?.message}")
            }

            override fun OnTokenRefreshed(accessToken: AccessToken?) {
                ILog.i(TAG, "refresh access token succeed:${accessToken}")
            }
        })
    }

    private fun checkProfile(): String? {
        try {
            return Profile.getCurrentProfile()?.let {
                if (it.id.isNullOrEmpty()) {
                    return@let null
                }
                return@let it.toJSONObject()?.toString()
            }
        } catch (_: Exception) {
            return null
        }
    }

    private fun requireUserInfo(responseClient: Boolean = false) {
        checkProfile()?.let {
            ILog.i(TAG, "got user info from profile:$it")
        } ?: AccessToken.getCurrentAccessToken()?.let { accessToken ->
            Utility.getGraphMeRequestWithCacheAsync(accessToken.token, object :
                GraphMeRequestWithCacheCallback {
                override fun onFailure(error: FacebookException?) {
                    ILog.i(TAG, "request me failed:${error?.message}")
                    if (responseClient) {
                        val message = if (error != null) error.message else "unknown"
                        authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, message)
                    }
                }

                override fun onSuccess(userInfo: JSONObject?) {
                    userInfo?.let { info ->
                        val id = info.optString("id")
                        when (id.isNullOrEmpty()) {
                            true -> {
                                ILog.i(TAG, "user id is null! invalid state")
                                if (responseClient) {
                                    authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "invalid response")
                                } else {

                                }
                            }

                            false -> {
                                val link = info.optString("link_uri")
                                val linkUri = if (link.isNullOrEmpty()) null else Uri.parse(link)
                                val picture = info.optString("picture_uri")
                                val pictureUri =
                                    if (picture.isNullOrEmpty()) null else Uri.parse(picture)
                                val profile = Profile(
                                    id,
                                    info.optString("first_name"),
                                    info.optString("middle_name"),
                                    info.optString("last_name"),
                                    info.optString("name"),
                                    linkUri,
                                    pictureUri
                                )
                                Profile.setCurrentProfile(profile)
                                if (responseClient) {
                                    authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, true)
                                } else {

                                }
                            }
                        }
                    } ?: {
                        if (responseClient) {
                            authResult?.onLoginResult(AuthPlatforms.LOGIN_PLATFORM_FACEBOOK, false, null, "invalid response")
                        }
                    }
                }
            })
        } ?: ILog.w(TAG, "load user info failed; access token invalid")
    }

    private fun requireFriends() {
        bundleOf("fields" to "picture,name,id").let { params ->
            GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/friends",
                params,
                HttpMethod.GET,
                { resp ->
                    try {
                        resp.jsonObject?.optJSONArray("data")?.let { data ->
                            for (i in 0 until data.length()) {
                                try {
                                    data.optJSONObject(i)?.let { item ->
                                        val url =
                                            item.optJSONObject("picture")?.optJSONObject("data")
                                                ?.optString("url") ?: ""
                                        item.put("picture", url)
                                    }
                                } catch (e: Exception) {
                                    ILog.e(TAG, "parse friend list item err:${e.message}")
                                }
                            }
                            friends = data.toString()
                        } ?: run {
                            ILog.i(TAG, "request friends list failed! invalid response")
                            friends = "[]"
                        }
                    } catch (_: Exception) {
                        ILog.i(TAG, "request friends list failed! invalid response")
                        friends = "[]"
                    }
                })
        }.executeAsync()
    }

}