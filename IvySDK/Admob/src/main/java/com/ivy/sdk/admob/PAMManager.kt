package com.ivy.sdk.admob

import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

data class PAM(val min: Double, val max: Double, val value: String)

class PAMManager : IPAMManager {

    companion object {
        const val TAG = "PAM"
    }

    private val bannerAds: MutableList<PAM> = mutableListOf()
    private val interstitialAds: MutableList<PAM> = mutableListOf()
    private val rewardedAds: MutableList<PAM> = mutableListOf()

    private fun format(json: JSONObject): PAM? {
        try {
            val range = json.getJSONArray("range")
            val min = range.getDouble(0)
            val max = range.getDouble(1)
            if (min >= max) return null
            val value = json.getString("value")
            return PAM(min, max, value)
        } catch (e: Exception) {
            ILog.e(TAG, "format item error:${e.message}")
        }
        return null
    }

    override fun setupPAMData(data: String?) {
        if (data == null) return
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val parent = JSONObject(data)
                parent.optJSONArray("rewarded")?.let { src ->
                    val count = src.length()
                    for (index in 0 until count) {
                        try {
                            format(src.getJSONObject(index))?.let { rewardedAds.add(it) }
                        } catch (e: Exception) {
                            ILog.e(TAG, "format item error:${e.message}")
                        }
                    }
                }
                parent.optJSONArray("interstitial")?.let { src ->
                    val count = src.length()
                    for (index in 0 until count) {
                        try {
                            format(src.getJSONObject(index))?.let { interstitialAds.add(it) }
                        } catch (e: Exception) {
                            ILog.e(TAG, "format item error:${e.message}")
                        }
                    }
                }
                parent.optJSONArray("banner")?.let { src ->
                    val count = src.length()
                    for (index in 0 until count) {
                        try {
                            format(src.getJSONObject(index))?.let { bannerAds.add(it) }
                        } catch (e: Exception) {
                            ILog.e(TAG, "format item error:${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                ILog.e(TAG, "format error:${e.message}")
            }
        }
    }

    override fun getPAM(adType: AdType, price: Double): Any? {
        try {
            return when (adType) {
                AdType.BANNER -> bannerAds.find { price >= it.min && price < it.max }
                AdType.INTERSTITIAL -> interstitialAds.find { price >= it.min && price < it.max }
                AdType.REWARDED -> rewardedAds.find { price >= it.min && price < it.max }
                else -> null
            }
        } catch (e: Exception) {
            ILog.e(TAG, "get pam error:${e.message}")
        }
        return null
    }

}