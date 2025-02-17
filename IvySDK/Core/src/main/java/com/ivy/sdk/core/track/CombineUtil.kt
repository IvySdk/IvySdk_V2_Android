package com.ivy.sdk.core.track

import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

data class Combine(
    val target: String,
    val value: Double,
    val operation: String,
    val days: Int,
    val repeat: Boolean = false,
    val events: List<String>? = null,
    val properties: List<Pair<String, String>>? = null
)

class CombineUtil {

    companion object {
        const val TAG = "Combine"
        const val ONE_DAY = 24 * 3600 * 1000
    }

    private val tasks: MutableList<Combine> = mutableListOf()

    fun setup(data: JSONObject) {
        try {
            data.keys().forEach { target ->
                data.optJSONObject(target)?.let { json ->
                    val operation = json.optString("op", ">=")
                    val value = json.optDouble("v", 0.0)
                    val days = json.optInt("d", 0)
                    if (days <= 0) {
                        ILog.e(TAG, "failed setup event! d can not less than 0")
                        return@let
                    }
                    val repeat = if (json.has("r")) json.optBoolean("r") else false
                    val events = json.optJSONArray("e")?.let { arr ->
                        val list: MutableList<String> = mutableListOf()
                        for (index in 0 until arr.length()) {
                            val value = arr.optString(index)
                            if (value.isNullOrEmpty()) {
                                continue
                            }
                            list.add(value)
                        }
                        list
                    }
                    val property = json.optJSONObject("p")?.let { arr ->
                        val list: MutableList<Pair<String, String>> = mutableListOf()
                        for (key in arr.keys()) {
                            val value = arr.optString(key)
                            if (value.isNullOrEmpty()) {
                                continue
                            }
                            val pair = Pair<String, String>(key, value)
                            list.add(pair)
                        }
                        list
                    }
                    if (events.isNullOrEmpty() && property.isNullOrEmpty()) {
                        ILog.w(TAG, "$target config failed!")
                    } else {
                        val combine = Combine(target, value, operation, days, repeat, events, property)
                        tasks.add(combine)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "setup config failed:${e.message}")
        }
    }

    fun check(event: String, eventParams: Map<String, Any>? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            for (item in tasks) {
                try {
                    val status = item.events?.let { events ->
                        events.contains(event) && checkCombinationEventProperties(item, eventParams)
                    } ?: checkCombinationEventProperties(item, eventParams)
                    if (status) {
                        val condition = checkCondition(item)
                        if (condition) {
                            launch(Dispatchers.Main) {
                                IvyTrack.Instance.logEvent(item.target, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_COMBINATION)
                            }
                            return@launch
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun checkCondition(item: Combine): Boolean {
        val cacheKey = "ev_${item.target}"
        val alreadySent = LocalStorage.Instance.containsKey(cacheKey)
        if (alreadySent) {
            return false
        }
        if (item.days == 0) {
            return false
        }
        val firstOpenTime = LocalStorage.Instance.decodeLong(IKeys.KEY_FIRST_OPEN_TIME, System.currentTimeMillis())
        //应用从安装开始的天数
        val openedDays = (System.currentTimeMillis() - firstOpenTime) / ONE_DAY + 1
        if (openedDays > item.days) {
            return false
        }
        val meetEventTimesKey = "ev_${item.target}_times"
        val meetEventTimes = LocalStorage.Instance.decodeInt(meetEventTimesKey, 0) + 1
        LocalStorage.Instance.encodeInt(meetEventTimesKey, meetEventTimes)
        var meetConditionTimesKey = 1
        val value = if (item.repeat) {
            meetConditionTimesKey = LocalStorage.Instance.decodeInt("ev_${item.target}_meet_times", 1)
            item.value * meetConditionTimesKey
        } else {
            item.value
        }
        val status: Boolean = when (item.operation) {
            ">=" -> meetEventTimes >= value
            "<" -> meetEventTimes < value
            ">" -> meetEventTimes > value
            "=" -> meetEventTimes == value.toInt()
            else -> {
                ILog.e(TAG, "invalid combine event operation!!!")
                false
            }
        }
        if (status) {
            if (item.repeat) {
                LocalStorage.Instance.encodeInt("ev_${item.target}_meet_times", meetConditionTimesKey + 1)
            }
            if (!item.repeat) {
                LocalStorage.Instance.encodeBoolean(cacheKey, true)
            }
            return true
        }
        return false
    }

    @Throws(Exception::class)
    private fun checkCombinationEventProperties(item: Combine, eventParams: Map<String, Any>? = null): Boolean {
        return item.properties?.let { properties ->
            eventParams?.let { params ->
                var isAccord = false
                for (property in properties) {
                    val paramsValue = params[property.first] as? String
                    if (paramsValue != null && paramsValue == property.second) {
                        isAccord = true
                        break
                    }
                }
                isAccord
            } ?: false
        } ?: true
    }

}