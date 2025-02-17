package com.ivy.sdk.remote.config

import org.json.JSONObject
import com.ivy.sdk.base.remote.IRCManager

open class IvyRemoteConfig private constructor() : IRCManager {

    private val manager: RCManager = RCManager()

    companion object {
        const val TAG = "ivyRemoteConfig"
        val Instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { IvyRemoteConfig() }
    }

    override fun check(
        appId: String,
        refreshInterval: Int,
        force: Boolean,
        appConfigCallback: (JSONObject) -> Unit,
        remoteConfigCallback: () -> Unit
    ) = manager.check(appId, refreshInterval, force, appConfigCallback, remoteConfigCallback)

    override fun setDefaultData(data: Map<String, Any>) = manager.setDefaultData(data)

    override fun clearCachedRemoteConfig() = manager.clearCachedRemoteConfig()

    override fun clearCachedAppConfig() = manager.clearCachedAppConfig()

    override fun getString(key: String): String? = manager.getString(key)

    override fun getBoolean(key: String): Boolean = manager.getBoolean(key)

    override fun getLong(key: String): Long = manager.getLong(key)

    override fun getDouble(key: String): Double = manager.getDouble(key)

    override fun getInt(key: String): Int = manager.getInt(key)


}