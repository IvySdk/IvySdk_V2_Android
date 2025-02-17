package com.ivy.sdk.remote.config

import com.ivy.sdk.base.App
import com.ivy.sdk.base.utils.Util
import org.json.JSONObject

internal object EncryptUtil {

    fun decrypt(src: String): String? = Util.decodeParams(App.Instance, src)

    fun isValidJson(data: String?): Pair<Boolean, JSONObject?> {
        try {
            val json = data?.let { JSONObject(it) }
            return when (json == null) {
                true -> Pair(false, null)
                false -> Pair(true, json)
            }
        } catch (e: Exception) {
            // e.printStackTrace()
        }
        return Pair(false, null)
    }
}