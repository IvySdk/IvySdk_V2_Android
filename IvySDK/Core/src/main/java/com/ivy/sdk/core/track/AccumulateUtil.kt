package com.ivy.sdk.core.track

import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


data class Accumulate(val target: String, val seq: List<Int>, val events: List<String>? = null, val properties: List<Pair<String, String>>? = null)

class AccumulateUtil {

    companion object {
        const val TAG = "Accumulate"
    }

    private var appOpenAccumulate: Accumulate? = null
    private var retentionAccumulate: Accumulate? = null

    private val tasks: MutableList<Accumulate> = mutableListOf()

    fun setup(data: JSONObject) {
        try {
            data.keys().forEach { key ->
                when (key) {
                    "op", "retention" -> {
                        data.optJSONArray(key)?.let { jsonArray ->
                            val seq: MutableList<Int> = mutableListOf()
                            for (index in 0 until jsonArray.length()) {
                                seq.add(jsonArray.optInt(index))
                            }
                            if (key == "op") {
                                appOpenAccumulate = Accumulate(key, seq)
                            }
                            if (key == "retention") {
                                retentionAccumulate = Accumulate(key, seq)
                            }
                        }
                    }

                    else -> {
                        //默认格式
                        try {
                            data.optJSONObject(key)?.let { json ->
                                val seq: MutableList<Int> = mutableListOf()
                                json.optJSONArray("count")?.let { jsonArray ->
                                    for (index in 0 until jsonArray.length()) {
                                        seq.add(jsonArray.optInt(index))
                                    }
                                }
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

                                if (seq.size > 0 && !(events.isNullOrEmpty() && property.isNullOrEmpty())) {
                                    val accumulate = Accumulate(key, seq, events, property)
                                    tasks.add(accumulate)
                                } else {
                                    ILog.w(TAG, "$key invalid accumulate config!")
                                }
                            }
                        } catch (e: Exception) {
                            ILog.w(TAG, "$key invalid accumulate config!::${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "failed parse Accumulate config:${e.message}")
        }
    }

    fun checkAppOpen(openTimes: Int) {
        checkOpenTimes(openTimes)
        checkRetention()
    }

    fun check(event: String, eventParams: Map<String, Any>? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            for (item in tasks) {
                try {
                    val status = item.events?.let { events ->
                        events.contains(event) && checkAccumulateEventProperties(item, eventParams)
                    } ?: checkAccumulateEventProperties(item, eventParams)
                    if (status) {
                        launch (Dispatchers.Main){
                            logAccumulateEvent(item)
                        }
                        return@launch
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    @Throws(Exception::class)
    private fun checkAccumulateEventProperties(item: Accumulate, eventParams: Map<String, Any>? = null): Boolean {
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

    @Throws(Exception::class)
    private fun logAccumulateEvent(item: Accumulate) {
        val newEventKey = "ev_${item.target}_count"
        val checkedCount = LocalStorage.Instance.decodeInt(newEventKey, 1)
        LocalStorage.Instance.encodeInt(newEventKey, checkedCount + 1)
        for (seq in item.seq) {
            if (seq == checkedCount) {
                val newEvent = "${item.target}_$checkedCount"
                if (LocalStorage.Instance.containsKey(newEvent)) {
                    ILog.i(TAG, "$newEvent already sent")
                } else {
                    LocalStorage.Instance.encodeBoolean(newEvent, true)
                    IvyTrack.Instance.logEvent(newEvent, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_COMBINATION)
                }
                return
            }
        }
    }

    private fun checkOpenTimes(openTimes: Int) {
        appOpenAccumulate?.let { config ->
            for (item in config.seq) {
                if (item == openTimes) {
                    val event = "op_$openTimes"
                    if (LocalStorage.Instance.containsKey(event)) {

                    } else {
                        LocalStorage.Instance.encodeBoolean(event, true)
                        IvyTrack.Instance.logEvent(event, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_COMBINATION)
                    }
                }
            }
        }
    }

    private fun checkRetention() {
        retentionAccumulate?.let { config ->
            val firstOpenTime = LocalStorage.Instance.decodeLong(IKeys.KEY_FIRST_OPEN_TIME, System.currentTimeMillis())
            val retentionDays = ((System.currentTimeMillis() - firstOpenTime) / IvyTrack.ONE_DAY_MILLISECONDS).toInt() + 1
            for (item in config.seq) {
                if (retentionDays == item) {
                    val event = "retention_$retentionDays"
                    if (LocalStorage.Instance.containsKey(event)) {

                    } else {
                        LocalStorage.Instance.encodeBoolean(event, true)
                        IvyTrack.Instance.logEvent(event, EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_COMBINATION)
                    }
                }
            }
        }
    }

}