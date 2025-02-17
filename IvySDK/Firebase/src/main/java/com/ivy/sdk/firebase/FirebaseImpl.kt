package com.ivy.sdk.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ivy.sdk.base.firebase.IFirebase

class FirebaseImpl : IFirebase {

    private var remoteConfig: FbRemoteConfig? = null

    init {
        FirebaseInit.initApp()
    }

    override fun initRemoteConfig(debug: Boolean, callback: (state: Boolean) -> Unit) {
        if (remoteConfig == null) {
            remoteConfig = FbRemoteConfig()
        }
        remoteConfig?.setup(debug, callback)
    }

    override fun setupDefaultRemoteData(defaultData: String?) {
        remoteConfig?.setupDefaultRemoteData(defaultData)
    }

    override fun getRemoteConfigString(key: String): String =
        remoteConfig?.getRemoteConfigString(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_STRING

    override fun getRemoteConfigDouble(key: String): Double =
        remoteConfig?.getRemoteConfigDouble(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_DOUBLE

    override fun getRemoteConfigBoolean(key: String): Boolean =
        remoteConfig?.getRemoteConfigBoolean(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_BOOLEAN

    override  fun getRemoteConfigLong(key: String): Long =
        remoteConfig?.getRemoteConfigLong(key) ?: FirebaseRemoteConfig.DEFAULT_VALUE_FOR_LONG


}