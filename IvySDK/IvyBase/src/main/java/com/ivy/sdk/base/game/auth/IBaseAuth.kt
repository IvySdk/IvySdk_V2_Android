package com.ivy.sdk.base.game.auth

import android.content.Intent
import org.json.JSONObject

interface IBaseAuth {

    fun setup(appId:String, jsonObject: JSONObject, debug: Boolean, authResult: IAuthResult)

    fun autoLogin()

    fun loadLoginStatus()

    fun login(authCallback: IAuthResponse? = null)

    fun logout()

    fun getLoginStatus(): Boolean

    fun getUserInfo(): String

    fun getUserId(): String

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

}