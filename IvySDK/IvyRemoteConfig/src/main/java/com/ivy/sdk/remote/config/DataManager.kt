package com.ivy.sdk.remote.config

import android.os.Build
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import com.ivy.sdk.remote.config.RCManager.Companion.KEY_LAST_REFRESH_TIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale


internal class DataManager {

    companion object {
        const val KEY_RC_DATA = "_rc_data_"
        const val KEY_SERVER_URL = "https://gameconfig.ivymobile.com/api/external/v1/distribution/remote_config/edata"
    }

    private val appConfig: AppConfigManager = AppConfigManager()

    fun loadRemoteData(appId: String, refreshInterval: Int, force: Boolean) {
        if (force) {
            loadRemoteConfigs(appId)
            return
        }
        val lastRefreshTime = RCLocalStorage.Instance.decodeLong(KEY_LAST_REFRESH_TIME, 0L)
        if ((System.currentTimeMillis() - lastRefreshTime) / 1000 / 60 > refreshInterval) {
            //已过期
            loadRemoteConfigs(appId)
            return
        }
        val allKeys = RCLocalStorage.Instance.allKeys()
        if (allKeys.isNullOrEmpty()) {
            loadRemoteConfigs(appId)
        }
    }

    fun loadAppConfig(appId: String, onResult: (JSONObject) -> Unit) {
        appConfig.loadLocalConfig {
            onResult.invoke(it)
            val vApi = it.optString("v_api")
            val url = getUrl(appId, true, vApi)
            appConfig.loadRemote(url)
        }
    }

    fun clearCachedAppConfig() {
        appConfig.clearCachedData()
    }

    private fun loadRemoteConfigs(appId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = getUrl(appId, false)
                RemoteDataTask.loadData(url)?.let {
                    val data = JSONObject(it).getString("data")
                    val deData = EncryptUtil.decrypt(data)
                    ILog.i(IvyRemoteConfig.TAG, "load remote configs:")
                    ILog.i(IvyRemoteConfig.TAG, "$deData")
                    val result = EncryptUtil.isValidJson(deData)
                    if (result.first && result.second != null) {
                        result.second?.let { json ->
                            val keys = json.keys()
                            for (key in keys) {
                                json.opt(key)?.let { v ->
                                    when (v) {
                                        is Long -> RCLocalStorage.Instance.encodeLong(key, v)
                                        is Int -> RCLocalStorage.Instance.encodeInt(key, v)
                                        is Double -> RCLocalStorage.Instance.encodeDouble(key, v)
                                        is Float -> RCLocalStorage.Instance.encodeFloat(key, v)
                                        is String -> RCLocalStorage.Instance.encodeString(key, v)
                                        else -> {
                                            try {
                                                RCLocalStorage.Instance.encodeString(key, v.toString())

                                            } catch (e: Exception) {
                                                ILog.w(IvyRemoteConfig.TAG, "error to format value  for key:$key")
                                            }
                                        }
                                    }
                                } ?: run { RCLocalStorage.Instance.removeValueForKey(key) }
                            }
                            if (json.length() > 0) {
                                RCLocalStorage.Instance.encodeLong(KEY_LAST_REFRESH_TIME, System.currentTimeMillis())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                ILog.e("RemoteConfig", "load remote data error:${e.message}")
            }
        }
    }

    private fun getUrl(appId: String, isAppConfig: Boolean, vApi: String? = ""): String {
        val userId = Util.roleId()
        val mediaSource = LocalStorage.Instance.decodeString(IKeys.KEY_AF_MEDIA_SOURCE, "")
        val country = Locale.getDefault().country
        val osVersion = Build.VERSION.SDK_INT
        val appVersion = Util.versionCode()
        val osRam = Util.byte2MB(Util.getTotalMemory())
        val url =
            "$KEY_SERVER_URL?app_id=$appId&user_id=$userId&media_source=$mediaSource&country=$country&os_version=$osVersion&app_version=$appVersion&os_ram=$osRam"
        if (isAppConfig) {
            return "$url&param=default_json&data_version=$vApi"
        }
        return url
    }

}