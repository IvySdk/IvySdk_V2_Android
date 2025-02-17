package com.ivy.sdk.base.remote

import org.json.JSONObject

interface IRCManager {

    fun check(appId: String, refreshInterval: Int, force:Boolean, appConfigCallback: (JSONObject) -> Unit, remoteConfigCallback: () -> Unit)

    fun setDefaultData(data: Map<String, Any>)

    fun clearCachedRemoteConfig()

    fun clearCachedAppConfig()

    fun getString(key: String): String?

    fun getBoolean(key: String): Boolean

    fun getLong(key: String): Long

    fun getDouble(key: String): Double

    fun getInt(key: String): Int


}