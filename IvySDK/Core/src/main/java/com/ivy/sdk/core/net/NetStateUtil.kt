package com.ivy.sdk.core.net

import android.content.Context
import android.net.ConnectivityManager
import com.ivy.sdk.base.App

/**
 *  监听网络状态需要获取 申请运行时权限 android.permission.ACCESS_NETWORK_STATE
 */
class NetStateUtil {

    companion object {

        fun isNetworkConnected(): Boolean {
            try {
                val connectivityManager = App.Instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = connectivityManager.activeNetworkInfo
                return netInfo != null && netInfo.isConnected
            } catch (e: Exception) {

            }
            return false
        }

    }

}