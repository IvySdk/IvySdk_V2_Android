package com.ivy.sdk.core

import com.ivy.sdk.base.App

class BaseApplication : App() {

    override fun setupAppConfig() {
        super.setupAppConfig()
        IvySdk.Instance.setupConfig()
    }


}