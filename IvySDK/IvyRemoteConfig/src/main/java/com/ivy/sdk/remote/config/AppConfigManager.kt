package com.ivy.sdk.remote.config

import android.os.Build
import com.ivy.sdk.base.App
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale


internal class AppConfigManager {

    companion object {
        const val TAG = "AppConfig"
    }

    fun loadLocalConfig(onResult: (JSONObject) -> Unit) {
        checkAndLoadConfig(onResult)
    }

    fun loadRemote(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RemoteDataTask.loadData(url)?.let { src ->
                    val data = JSONObject(src).getString("data")
                    val deData = EncryptUtil.decrypt(data)
                    val result = EncryptUtil.isValidJson(deData)
                    if (result.first && result.second != null && result.second?.getJSONObject("default_json") != null) {
                        cacheConfig(data)
                    }
                }
            } catch (e: Exception) {
                ILog.e(TAG, "load remote error:${e.message}")
            }
        }
    }

    fun clearCachedData() {
        try {
            File(App.Instance.cacheDir, "grid").delete()
        } catch (e: Exception) {
            ILog.w(TAG, "read cached grid failed:${e.message}")
        }
    }

    private fun checkAndLoadConfig(onResult: (JSONObject) -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val lastCachedVersionCode = LocalStorage.Instance.decodeLong("gridDataVersion", 0)
                val currentVersionCode = Util.versionCode()
                var enData: String? = if (lastCachedVersionCode == currentVersionCode) {
                    ILog.i(TAG, "read cache grid data")
                    loadCacheConfigFile()
                } else null
                var gridData: String? = null
                if (enData != null) {
                    //解析缓存
                    ILog.i(TAG, "found cache grid! start parse")
                    gridData = EncryptUtil.decrypt(enData)
                    try {
                        val json = JSONObject(gridData)
                        if (json.has("default_json")) {
                            gridData = json.getString("default_json")
                        }
                    } catch (_: Exception) {
                        gridData = null
                    }
                }
                if (gridData == null) {
                    //从assets读取分国家
                    gridData = loadCountryConfig()?.let { countryConfigFileName ->
                        return@let loadAssets(countryConfigFileName)?.let { en ->
                            enData = en
                            EncryptUtil.decrypt(en)
                        } ?: run {
                            ILog.w(TAG, "had not found country config file:$countryConfigFileName")
                            null
                        }
                    } ?: run {
                        ILog.w(TAG, "unable find country config")
                        null
                    }
                }
                val pair = EncryptUtil.isValidJson(gridData)
                if (pair.first) {
                    ILog.w(TAG, "valid grid data success")
                    launch(Dispatchers.Main) {
                        onResult(pair.second!!)
                    }
                    cacheConfig(enData)
                } else {
                    throw FileNotFoundException("unable find app config file")
                }
            } catch (e: Exception) {
                ILog.w(TAG, "load config failed!${e.message}")
                ILog.w(TAG, "start load default")
                loadDefaultConfig()?.let {
                    launch(Dispatchers.Main) {
                        onResult(it)
                    }
                } ?: {
                    launch(Dispatchers.Main) {
                        onResult(JSONObject())
                    }
                }
            }
        }
    }

    /**
     *  {
     *        "minAppVersion":1,                               //应用版本
     *        "minOSVersion":28,                               //系统版本
     *        "countries":["CN", "US"],                        //国家
     *        "OSRams":[0, 1000],                              // 内存区间
     *        "mediaSources": [],                              // 广告源
     *        "campaignIds":["applovinmax", "google_admob"],   // 广告源
     *        "deviceCategory":["huawei", "vivo"],             //设备类型， 默认 all
     *        "livingDays":[],                                 //活跃天数
     *       "configFileName":""                               //配置文件名称
     *     }
     */
    private fun loadCountryConfig(): String? {
        val fileName = "sdk_cg_${Util.packageName()}"
        val encryptFileName = Util.md5(fileName)
        ILog.i(TAG, "cg file name:$encryptFileName")
        return loadAssets(encryptFileName)?.let { EncryptUtil.decrypt(it) }?.let { deData ->
            var currentConfigFileName: String? = null
            try {
                val data = JSONArray(deData)
                val length = data.length()
                if (length <= 0) {
                    ILog.w(TAG, "invalid country config")
                    return@let null
                }
                val firstOpenTime = LocalStorage.Instance.decodeLong(IKeys.KEY_FIRST_OPEN_TIME, System.currentTimeMillis())
                val livingDays = (System.currentTimeMillis() - firstOpenTime) / (24 * 60 * 60 * 1000) + 1
                ILog.i(TAG, "current living days:$livingDays")
                val appVersion = Util.versionCode()
                ILog.i(TAG, "current app version:$appVersion")
                val osVersion = Build.VERSION.SDK_INT
                ILog.i(TAG, "current device system version:$osVersion")
                val country = Locale.getDefault().country
                ILog.i(TAG, "current country:$country")
                val byteMemory = Util.getTotalMemory()
                val mbMemory = Util.byte2MB(byteMemory)
                ILog.i(TAG, "current device total memory:${mbMemory}MB")
                val deviceBrand = Build.BRAND.lowercase(Locale.ENGLISH)
                ILog.i(TAG, "current device brand:$deviceBrand")
                val afCampaignId = LocalStorage.Instance.decodeString(IKeys.KEY_AF_CAMPAIGN_ID)
                ILog.i(TAG, "current user campaign:$afCampaignId")
                val afMediaSource = LocalStorage.Instance.decodeString(IKeys.KEY_AF_MEDIA_SOURCE)
                ILog.i(TAG, "current user media source:$afMediaSource")

                var validCondition: Int = 0
                for (index in 0 until length) {
                    var currentValidCondition = 0
                    val item = data.optJSONObject(index)
                    if (item.has("minAppVersion")) {
                        val minAppVersion = item.optInt("minAppVersion", 0)
                        if (appVersion >= minAppVersion) {
                            currentValidCondition++
                        } else {
                            continue
                        }
                    }
                    if (item.has("minOSVersion")) {
                        val minOSVersion = item.optInt("minOSVersion", 0)
                        if (osVersion >= minOSVersion) {
                            currentValidCondition++
                        } else {
                            continue
                        }
                    }
                    if (item.has("countries")) {
                        val countries = item.optJSONArray("countries")?.let {
                            val list: MutableList<String> = mutableListOf()
                            for (i in 0 until it.length()) {
                                list.add(it.optString(i, ""))
                            }
                            list
                        }
                        if (!countries.isNullOrEmpty()) {
                            if (countries.contains(country)) {
                                currentValidCondition++
                            } else {
                                continue
                            }
                        }
                    }
                    if (item.has("OSRams")) {
                        val ramState = item.optJSONArray("countries")?.let {
                            if (it.length() == 2) {
                                val minRam = it.optLong(0, 0)
                                val maxRam = it.optLong(1, 0)
                                val result: Boolean = mbMemory >= minRam && mbMemory < maxRam
                                return@let result
                            }
                            return@let true
                        } ?: true
                        if (ramState) {
                            currentValidCondition++
                        } else {
                            continue
                        }
                    }
                    if (!afMediaSource.isNullOrEmpty() && item.has("mediaSources")) {
                        val mediaSources = item.optJSONArray("mediaSources")?.let {
                            val list: MutableList<String> = mutableListOf()
                            for (i in 0 until it.length()) {
                                list.add(it.optString(i, ""))
                            }
                            list
                        }
                        if (!mediaSources.isNullOrEmpty()) {
                            if (mediaSources.contains(afMediaSource)) {
                                currentValidCondition++
                            } else {
                                continue
                            }
                        }
                    }
                    if (!afCampaignId.isNullOrEmpty() && item.has("campaignIds")) {
                        val campaignIds = item.optJSONArray("campaignIds")?.let {
                            val list: MutableList<String> = mutableListOf()
                            for (i in 0 until it.length()) {
                                list.add(it.optString(i, ""))
                            }
                            list
                        }
                        if (!campaignIds.isNullOrEmpty()) {
                            if (campaignIds.contains(afCampaignId)) {
                                validCondition++
                            } else {
                                continue
                            }
                        }
                    }
                    if (item.has("livingDays")) {
                        val livingDayState = item.optJSONArray("livingDays")?.let {
                            for (i in 0 until it.length()) {
                                val itemValue = it.optLong(i)
                                return@let itemValue == livingDays
                            }
                            return@let true
                        } ?: true
                        if (livingDayState) {
                            currentValidCondition++
                        } else {
                            continue
                        }
                    }
                    val currentFile = item.optString("configFileName", "")
                    if (!currentFile.isNullOrEmpty()) {
                        if (currentValidCondition >= validCondition) {
                            currentConfigFileName = currentFile
                        }
                    }
                }
            } catch (_: Exception) {
            }
            currentConfigFileName
        } ?: run {
            ILog.w(TAG, "no country config found")
            null
        }
    }

    private fun loadAssets(fileName: String): String? {
        try {
            val input = App.Instance.assets.open(fileName)
            val byteArray = ByteArray(input.available())
            input.read(byteArray)
            return String(byteArray)
        } catch (e: Exception) {
            ILog.e(TAG, "read assets $fileName failed;${e.message}")
            //e.printStackTrace()
        }
        return null
    }

    private fun loadCacheConfigFile(): String? {
        try {
            return File(App.Instance.cacheDir, "grid").readText()
        } catch (e: Exception) {
            ILog.w(TAG, "read cached grid failed:${e.message}")
        }
        return null
    }

    private fun cacheConfig(data: String?) {
        try {
            if (data == null) return
            File(App.Instance.cacheDir, "grid").writeText(data)
            val version = Util.versionCode()
            LocalStorage.Instance.encodeLong("gridDataVersion", version)
        } catch (e: Exception) {
            ILog.w(TAG, "cache grid failed:${e.message}")
        }
    }

    private fun loadDefaultConfig(): JSONObject? {
        try {
            val data = loadAssets("default.json")
            return JSONObject(data)
        } catch (e: Exception) {
            ILog.w(TAG, "load default config failed:${e.message}")
        }
        return null
    }


}