package com.ivy.sdk.base.firebase

interface IFirebase {

    fun initRemoteConfig(debug: Boolean, callback: (state: Boolean) -> Unit)

    fun setupDefaultRemoteData(defaultData: String?)

    fun getRemoteConfigString(key: String): String

    fun getRemoteConfigDouble(key: String): Double

    fun getRemoteConfigBoolean(key: String): Boolean

    fun getRemoteConfigLong(key: String): Long

}