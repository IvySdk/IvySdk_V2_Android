//package com.ivy.sdk.games.google
//
//import androidx.credentials.ClearCredentialStateRequest
//import androidx.credentials.CredentialManager
//import androidx.credentials.CredentialManagerCallback
//import androidx.credentials.CustomCredential
//import androidx.credentials.GetCredentialRequest
//import androidx.credentials.GetCredentialResponse
//import androidx.credentials.exceptions.ClearCredentialException
//import com.google.android.libraries.identity.googleid.GetGoogleIdOption
//import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//import com.ivy.sdk.base.App
//import com.ivy.sdk.base.utils.ILog
//import com.ivy.sdk.games.ILoginResult
//import com.ivy.sdk.games.LoginPlatforms
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import java.util.concurrent.Executors
//
///**
// * google sign credentials
// * doc: https://developer.android.com/identity/sign-in/credential-manager-siwg?hl=zh-cn
// *
// *
// * 1. 通过将 setFilterByAuthorizedAccounts 参数设置为 true 调用 API，检查用户是否有任何之前用于登录应用的账号。
// * 用户可以从可用账号中选择一个账号进行登录。
// *
// * 如果没有可用的已获授权的 Google 账号，系统应提示用户使用其任何可用的账号进行注册。
// * 为此，请再次调用 API 并将 setFilterByAuthorizedAccounts 设置为 false，以提示用户
// *
// * 2. 为回访用户启用自动登录功能
// * 如需启用自动登录功能，请使用 setAutoSelectEnabled(true)。只有在满足以下条件时，用户才能自动登录：
// *
// * 只有一个凭据与请求匹配，该凭据可以是 Google 账号或密码，并且该凭据与 Android 设备上的默认账号匹配。
// * 用户尚未明确退出账号。
// * 用户尚未在其 Google 账号设置中停用自动登录功能。
// *
// *
// */
//class GoogleSignImpl : IIGoogleSign {
//
//    companion object {
//        const val TAG = "Google_SIGN"
//    }
//
//    private var callback: ILoginResult? = null
//    private var web_client_id: String? = null
//    private var credentialManager: CredentialManager? = null
//    private var googleIdTokenCredential: GoogleIdTokenCredential? = null
//
//    private fun getCredentialManager(): CredentialManager = credentialManager ?: run {
//        credentialManager = CredentialManager.create(App.Instance)
//        return@run credentialManager!!
//    }
//
//    override fun setup(config: JSONObject, loginCallback: ILoginResult, debug: Boolean) {
//        this.callback = loginCallback
//        web_client_id = config.optString("web_client_id")
//        autoLogin()
//    }
//
//    override fun autoLogin() {
//        if (web_client_id.isNullOrEmpty()) {
//            ILog.e(TAG, "no web client id configured! checkout it!!!")
//            // callback?.failure(LoginPlatforms.LOGIN_PLATFORM_GOOGLE, "no web client id configured!")
//            return
//        }
//        val googleIdOption = createGoogleIdSignOption(true)
//        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val result: GetCredentialResponse = getCredentialManager().getCredential(App.Instance, request)
//                handleLoginResult(result, true)
//            } catch (e: Exception) {
//                ILog.e(TAG, "auto login failed:${e.message}")
//            }
//        }
//    }
//
//    //auto login 不回传登录状态
//    private fun handleLoginResult(result: GetCredentialResponse, fromAutoLogin: Boolean = false) {
//        when (val credential = result.credential) {
//            is CustomCredential -> {
//                try {
//                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
//                        googleIdTokenCredential =
//                            GoogleIdTokenCredential
//                                .createFrom(credential.data)
//                        if (!fromAutoLogin) {
//                            callback?.onLoginResult(true, LoginPlatforms.LOGIN_PLATFORM_GOOGLE)
//                        }
//                    } else {
//                        throw IllegalStateException("unexpected credential type:${credential.type}")
//                    }
//                } catch (e: Exception) {
//                    if (!fromAutoLogin) {
//                        ILog.e(TAG, "login failed:${e.message}")
//                        callback?.onLoginResult(false, LoginPlatforms.LOGIN_PLATFORM_GOOGLE, "Unexpected type of credential")
//                    } else {
//                        ILog.i(TAG, "login failed:${e.message}")
//                    }
//                }
//            }
//
//            else -> {
//                if (!fromAutoLogin) {
//                    ILog.e(TAG, "Unexpected type of credential")
//                    callback?.onLoginResult(
//                        false,
//                        LoginPlatforms.LOGIN_PLATFORM_GOOGLE,
//                        "Unexpected type of credential"
//                    )
//                } else {
//                    ILog.i(TAG, "Unexpected type of credential")
//                }
//            }
//        }
//    }
//
//    override fun isGoogleLogged(): Boolean {
//        return googleIdTokenCredential != null && !googleIdTokenCredential!!.idToken.isNullOrEmpty()
//    }
//
//    override fun loginGoogle() {
//        if (web_client_id.isNullOrEmpty()) {
//            ILog.e(TAG, "no web client id configured! checkout it!!!")
//            callback?.onLoginResult(false, LoginPlatforms.LOGIN_PLATFORM_GOOGLE, "no web client id configured!")
//            return
//        }
//        val activity = App.Instance.activity
//        if (activity == null){
//            ILog.e(TAG, "invalid activity to sign")
//            callback?.onLoginResult(false, LoginPlatforms.LOGIN_PLATFORM_GOOGLE, "invalid activity instance")
//            return
//        }
//        val googleIdOption = createGoogleIdSignOption(false)
//        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val result: GetCredentialResponse = getCredentialManager().getCredential(activity, request)
//                handleLoginResult(result, false)
//            } catch (e: Exception) {
//                launch(Dispatchers.Main) {
//                    callback?.onLoginResult(true, LoginPlatforms.LOGIN_PLATFORM_GOOGLE, "${e.message}")
//                }
//            }
//        }
//    }
//
//    override fun logoutGoogle() {
//        val request = ClearCredentialStateRequest()
//        getCredentialManager().clearCredentialStateAsync(
//            request,
//            null,
//            Executors.newSingleThreadExecutor(),
//            object : CredentialManagerCallback<Void?, ClearCredentialException> {
//                override fun onError(e: ClearCredentialException) {
//                    ILog.w(TAG, "logout failed:${e.message}")
//                }
//
//                override fun onResult(result: Void?) {
//                    ILog.d(TAG, "logout success")
//                    callback?.onLogout(LoginPlatforms.LOGIN_PLATFORM_GOOGLE)
//                }
//            })
//    }
//
//    override fun getLoggedGoogleUserInfo(): String {
//        return googleIdTokenCredential?.let {
//            try {
//                val json = JSONObject()
//                json.put("id", it.id)
//                json.put("idToken", it.idToken)
//                json.put("name", it.displayName ?: "")
//                json.put("photo", it.profilePictureUri?.toString() ?: "")
//                json.put("phone", it.phoneNumber ?: "")
//                return@let json.toString()
//            } catch (e: Exception) {
//                ILog.i(TAG, "format user info err:${e.message}")
//                return@let "{}"
//            }
//        } ?: run {
//            ILog.i(TAG, "no user logged to get user info")
//            return@run "{}"
//        }
//    }
//
//    /**
//     * google 账号登录
//     */
//    private fun createGoogleSignOption(
//        nonce: String? = null
//    ): GetSignInWithGoogleOption {
//        val googleOptionBuilder: GetSignInWithGoogleOption.Builder =
//            GetSignInWithGoogleOption.Builder(web_client_id!!)
//        nonce?.let { googleOptionBuilder.setNonce(it) }
//        return googleOptionBuilder.build()
//    }
//
//    /**
//     * Google id 登录
//     */
//    private fun createGoogleIdSignOption(
//        filterByAuthorizedAccounts: Boolean,
//        autoSelectEnabled: Boolean = true,
//        nonce: String? = null
//    ): GetGoogleIdOption {
//        val googleIdOptionBuilder: GetGoogleIdOption.Builder = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
//            .setAutoSelectEnabled(autoSelectEnabled)
//        if (!web_client_id.isNullOrEmpty()) {
//            googleIdOptionBuilder.setServerClientId(web_client_id!!)
//        }
//        nonce?.let { googleIdOptionBuilder.setNonce(it) }
//        return googleIdOptionBuilder.build()
//    }
//
//}