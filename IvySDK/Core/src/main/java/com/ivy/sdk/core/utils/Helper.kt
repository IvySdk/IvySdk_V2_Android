package com.ivy.sdk.core.utils

import android.net.Uri
import com.ivy.sdk.base.grid.GridManager
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import com.ivy.sdk.core.IvySdk
import java.util.Locale

class Helper {

    companion object {
        const val CONFIG_KEY_APP_ID: Int = 1
        const val CONFIG_KEY_LEADER_BOARD_URL: Int = 2
        const val CONFIG_KEY_API_VERSION: Int = 3
        const val CONFIG_KEY_SCREEN_WIDTH: Int = 4
        const val CONFIG_KEY_SCREEN_HEIGHT: Int = 5
        const val CONFIG_KEY_LANGUAGE: Int = 6
        const val CONFIG_KEY_COUNTRY: Int = 7
        const val CONFIG_KEY_VERSION_CODE: Int = 8
        const val CONFIG_KEY_VERSION_NAME: Int = 9
        const val CONFIG_KEY_PACKAGE_NAME: Int = 10
        const val CONFIG_KEY_UUID: Int = 11
        const val SDK_CONFIG_KEY_JSON_VERSION: Int = 21

        fun getConfig(key: Int): String? {
            try {
                return when (key) {
                    CONFIG_KEY_APP_ID -> GridManager.Instance.getGridConfig("appid", "")
                    CONFIG_KEY_LEADER_BOARD_URL -> GridManager.Instance.getGridConfig("leader_board_url", "")
                    CONFIG_KEY_API_VERSION -> GridManager.Instance.getGridConfig("v_api", "26")
                    CONFIG_KEY_SCREEN_WIDTH -> Util.screenWith().toString()
                    CONFIG_KEY_SCREEN_HEIGHT -> Util.screenHeight().toString()
                    CONFIG_KEY_LANGUAGE -> Locale.getDefault().language
                    CONFIG_KEY_COUNTRY -> Locale.getDefault().country
                    CONFIG_KEY_VERSION_CODE -> Util.versionCode().toString()
                    CONFIG_KEY_VERSION_NAME -> Util.versionName()
                    CONFIG_KEY_PACKAGE_NAME -> Util.packageName()
                    CONFIG_KEY_UUID -> Util.roleId()
                    SDK_CONFIG_KEY_JSON_VERSION -> {
                        val domain = GridManager.Instance.getGridConfig("domain", "")
                        return domain?.let {
                            Uri.parse(it)?.getQueryParameter("v_api") ?: ""
                        } ?: ""
                    }

                    else -> ""
                }
            } catch (e: Exception) {
                ILog.e(IvySdk.TAG, "get app config err:${e.message}")
            }
            return ""
        }
    }


}