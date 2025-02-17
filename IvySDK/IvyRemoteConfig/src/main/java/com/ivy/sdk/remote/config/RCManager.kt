package com.ivy.sdk.remote.config

import com.ivy.sdk.base.App
import com.ivy.sdk.base.remote.IRCManager
import org.json.JSONObject

internal class RCManager() : IRCManager {


    companion object {
        const val KEY_LAST_REFRESH_TIME = "_rc_last_refresh_time_"
    }

    init {
        RCLocalStorage.Instance.initLocalStorage(App.Instance, "ivy.remote.config")
    }

    private val dataManager: DataManager = DataManager()
    private val defaultData: MutableMap<String, Any> = mutableMapOf()

    override fun check(
        appId: String,
        refreshInterval: Int,
        force: Boolean,
        appConfigCallback: (JSONObject) -> Unit,
        remoteConfigCallback: () -> Unit
    ) {
        dataManager.loadAppConfig(appId, appConfigCallback)
        dataManager.loadRemoteData(appId, refreshInterval, force)
    }

    override fun setDefaultData(data: Map<String, Any>) {
        this.defaultData.putAll(data)
    }

    override fun clearCachedRemoteConfig() {
        RCLocalStorage.Instance.clearAll()
    }

    override fun clearCachedAppConfig() {
        dataManager.clearCachedAppConfig()
    }

    override fun getString(key: String): String? {
        if (RCLocalStorage.Instance.contains(key)) {
            return RCLocalStorage.Instance.decodeString(key, null)
        }
        if (defaultData.containsKey(key)) {
            val value = defaultData[key]
            if (value is String) {
                return value
            }
        }
        return null
    }

    override fun getBoolean(key: String): Boolean {
        if (RCLocalStorage.Instance.contains(key)) {
            return RCLocalStorage.Instance.decodeBoolean(key, false)
        }
        if (defaultData.containsKey(key)) {
            val value = defaultData[key]
            if (value is Boolean) {
                return value
            }
        }
        return false
    }

    override fun getLong(key: String): Long {
        if (RCLocalStorage.Instance.contains(key)) {
            return RCLocalStorage.Instance.decodeLong(key, 0L)
        }
        if (defaultData.containsKey(key)) {
            val value = defaultData[key]
            if (value is Long) {
                return value
            }
        }
        return 0L
    }

    override fun getDouble(key: String): Double {
        if (RCLocalStorage.Instance.contains(key)) {
            return RCLocalStorage.Instance.decodeDouble(key, 0.0)
        }
        if (defaultData.containsKey(key)) {
            val value = defaultData[key]
            if (value is Double) {
                return value
            }
        }
        return 0.0
    }

    override fun getInt(key: String): Int {
        if (RCLocalStorage.Instance.contains(key)) {
            return RCLocalStorage.Instance.decodeInt(key, 0)
        }
        if (defaultData.containsKey(key)) {
            val value = defaultData[key]
            if (value is Int) {
                return value
            }
        }
        return 0
    }



}