package com.ivy.sdk.firebase

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

import com.ivy.sdk.base.utils.ILog
import org.json.JSONObject

internal class FbRemoteConfig {

    companion object {
        const val TAG = "FireRemote"
    }

    private var remoteConfig: FirebaseRemoteConfig? = null

    fun setup(debug: Boolean, callback: (state: Boolean) -> Unit) {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (debug) 1 * 60 * 60 else 24 * 60 * 60
        }
        remoteConfig!!.setConfigSettingsAsync(configSettings)
        remoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
            callback.invoke(task.isSuccessful)
        }
    }

    fun setupDefaultRemoteData(defaultData: String?) {
        defaultData?.let { data ->
            try {
                val map: MutableMap<String, Any> = mutableMapOf()
                val json = JSONObject(data)
                json.keys().forEach { key ->
                    map[key] = json.opt(key) ?: ""
                }
                remoteConfig?.setDefaultsAsync(map)
                    ?: throw IllegalAccessException("had not call init firebase remote config before use")
            } catch (e: Exception) {
                ILog.e(TAG, "setup default remote config data err:${e.message}")
            }
        }
    }

    fun getRemoteConfigString(key: String): String =
        remoteConfig?.getString(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING

    fun getRemoteConfigDouble(key: String): Double =
        remoteConfig?.getDouble(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_DOUBLE

    fun getRemoteConfigBoolean(key: String): Boolean =
        remoteConfig?.getBoolean(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_BOOLEAN

    fun getRemoteConfigLong(key: String): Long =
        remoteConfig?.getLong(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_LONG


}