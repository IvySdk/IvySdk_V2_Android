package com.ivy.sdk.core.track

import android.os.Bundle
import com.ivy.sdk.base.net.HttpUtil
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.core.IvySdk
import com.ivy.sdk.core.utils.Helper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UacUtil constructor(private val appId: String, private val apiUrl: String) {

    companion object {

        const val TAG = "UAC"

        const val KEY_UAC_DAY3_TOP10: String = "_uac_day3_top_10"
        const val KEY_UAC_DAY3_TOP20: String = "_uac_day3_top_20"
        const val KEY_UAC_DAY3_TOP30: String = "_uac_day3_top_30"
        const val KEY_UAC_TOP10: String = "_uac_top_10"
        const val KEY_UAC_TOP20: String = "_uac_top_20"
        const val KEY_UAC_TOP30: String = "_uac_top_30"
        const val KEY_UAC_TOP40: String = "_uac_top_40"
        const val KEY_UAC_TOP50: String = "_uac_top_50"
        const val KEY_UAC_TOP60: String = "_uac_top_60"
        const val KEY_UAC_TOP70: String = "_uac_top_70"
        const val KEY_UAC_TOP80: String = "_uac_top_80"
        const val KEY_UAC_TOP90: String = "_uac_top_90"
        const val KEY_UAC_UPDATE_TIME: String = "_uac_update_ts"
        const val KEY_UAC_PREFIX_TODAY: String = "_uac_rev_"
        const val PREFIX_EVENT_UAC: String = "AdLtv_OneDay_Top"

    }

    private var todayKey: String = ""

    init {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        todayKey = simpleDateFormat.format(date)
    }

    open fun checkAndUpdateUacTop() {
        val lastCheckTs: Long = LocalStorage.Instance.decodeLong(KEY_UAC_UPDATE_TIME, 0L)
        if (System.currentTimeMillis() - lastCheckTs < 2 * 3600 * 1000L) {
            return
        }
        val pkgName = Helper.getConfig(Helper.CONFIG_KEY_PACKAGE_NAME)
        val request = Request.Builder().url("$apiUrl=$appId&packageName=$pkgName").get().build()
        HttpUtil.Instance.okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ILog.i(TAG, "checkAndUpdateUacTop err:${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.body?.string()?.let { data ->
                        val result = JSONObject(data)
                        val status = result.getString("status")
                        if ("success" != status) {
                            ILog.i(TAG, "checkAndUpdateUacTop result status not success")
                            return
                        }
                        val topData = result.optJSONObject("data")
                        if (topData != null) {
                            val top10 = topData.optDouble("t10")
                            val top20 = topData.optDouble("t20")
                            val top30 = topData.optDouble("t30")
                            val top40 = topData.optDouble("t40")
                            val top50 = topData.optDouble("t50")
                            val top60 = topData.optDouble("t60")
                            val top70 = topData.optDouble("t70")
                            val top80 = topData.optDouble("t80")
                            val top90 = topData.optDouble("t90")
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP10, top10)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP20, top20)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP30, top30)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP40, top40)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP50, top50)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP60, top60)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP70, top70)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP80, top80)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_TOP90, top90)

                            LocalStorage.Instance.encodeLong(KEY_UAC_UPDATE_TIME, System.currentTimeMillis())
                        }

                        val top3Data = result.optJSONObject("day3_data")
                        if (top3Data != null) {
                            val day3Top30 = top3Data.optDouble("t30")
                            val day3Top20 = top3Data.optDouble("t20")
                            val day3Top10 = top3Data.optDouble("t10")
                            LocalStorage.Instance.encodeDouble(KEY_UAC_DAY3_TOP10, day3Top10)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_DAY3_TOP20, day3Top20)
                            LocalStorage.Instance.encodeDouble(KEY_UAC_DAY3_TOP30, day3Top30)
                        }
                    } ?: throw IllegalArgumentException("invalid response data")
                } catch (e: Exception) {
                    ILog.i(TAG, "checkAndUpdateUacTop result parse err:${e.message}")
                }
            }
        })
    }

    private fun logUacConversionEvent(top: Int, dayRevenue: Double, totalRevenue: Double) {
        val sendFlagKey = (KEY_UAC_PREFIX_TODAY + todayKey + top).toString() + "_gen"
        val alreadySent: Boolean = LocalStorage.Instance.decodeBoolean(sendFlagKey, false)
        if (alreadySent) {
            return
        }
        val params = mutableMapOf<String, Any>("total_revenue" to totalRevenue, "day_revenue" to dayRevenue, "label" to todayKey)
        val eventName = PREFIX_EVENT_UAC + top
        IvyTrack.Instance.logEvent(eventName, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        LocalStorage.Instance.encodeBoolean(sendFlagKey, true)
    }

    open fun checkFirstThreeDaysLTV(totalRevenue: Float) {
        val duration: Long = System.currentTimeMillis() - IvyTrack.Instance.getFirstOpenTime()
        if (duration <= 0) return
        val hours = (duration / 1000 / 60 / 60).toInt()
        if (hours > 72) return
        val day3Top30: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_DAY3_TOP30, 0.0)
        if (day3Top30 > 0) {
            if (totalRevenue >= day3Top30) {
                logFirstThreeDaysLTV("30", totalRevenue, hours)
            } else {
                return
            }
        }
        val day3Top20: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_DAY3_TOP20, 0.0)
        if (day3Top20 > 0) {
            if (totalRevenue >= day3Top20) {
                logFirstThreeDaysLTV("20", totalRevenue, hours)
            } else {
                return
            }
        }
        val day3Top10: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_DAY3_TOP10, 0.0)
        if (day3Top10 > 0) {
            if (totalRevenue >= day3Top10) {
                logFirstThreeDaysLTV("10", totalRevenue, hours)
            } else {
                return
            }
        }
    }

    private fun logFirstThreeDaysLTV(tag: String, totalRevenue: Float, hours: Int) {
        val sendFlag = "AdLtv_day3_top" + tag + "_gen"
        val alreadySent: Boolean = LocalStorage.Instance.decodeBoolean(sendFlag, false)
        if (alreadySent) return
        val bundle = Bundle()
        bundle.putDouble("total_revenue", totalRevenue.toDouble())
        bundle.putInt("hours", hours)
        bundle.putString("catalog", "day3")
        val eventName = "AdLtv_day3_top$tag"
        val params = mutableMapOf<String, Any>("total_revenue" to totalRevenue, "hours" to hours, "catalog" to "day3")
        IvyTrack.Instance.logEvent(eventName, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_SDK, params)
        LocalStorage.Instance.encodeBoolean(sendFlag, true)
    }

    /**
     * 获取当前日期(UTC+0)并将广告收入累积到当日的KEY上
     * 1. 检查日是否触发topN，逐一生成对应事件到firebase和af
     *
     * @param revenue
     */
    open fun checkUacLtvConversion(revenue: Float, totalRevenue: Float) {
        val todayRevenue: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_PREFIX_TODAY + todayKey, 0.0) + revenue
        LocalStorage.Instance.encodeDouble(KEY_UAC_PREFIX_TODAY + todayKey, todayRevenue)

        // 检查是否生成adLtv事件
        val top90: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP90, 0.0)
        if (top90 > 0) {
            if (todayRevenue >= top90) {
                logUacConversionEvent(90, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top80: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP80, 0.0)
        if (top80 > 0) {
            if (todayRevenue >= top80) {
                logUacConversionEvent(80, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top70: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP70, 0.0)
        if (top70 > 0) {
            if (todayRevenue >= top70) {
                logUacConversionEvent(70, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top60: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP60, 0.0)
        if (top60 > 0) {
            if (todayRevenue >= top60) {
                logUacConversionEvent(60, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top50: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP50, 0.0)
        if (top50 > 0) {
            if (todayRevenue >= top50) {
                logUacConversionEvent(50, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top40: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP40, 0.0)
        if (top40 > 0) {
            if (todayRevenue >= top40) {
                logUacConversionEvent(40, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top30: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP30, 0.0)
        if (top30 > 0) {
            if (todayRevenue >= top30) {
                logUacConversionEvent(30, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top20: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP20, 0.0)
        if (top20 > 0) {
            if (todayRevenue >= top20) {
                logUacConversionEvent(20, todayRevenue, totalRevenue.toDouble())
            } else {
                return
            }
        }

        val top10: Double = LocalStorage.Instance.decodeDouble(KEY_UAC_TOP10, 0.0)
        if (top10 > 0) {
            if (todayRevenue >= top10) {
                logUacConversionEvent(10, todayRevenue, totalRevenue.toDouble())
            }
        }
    }


}