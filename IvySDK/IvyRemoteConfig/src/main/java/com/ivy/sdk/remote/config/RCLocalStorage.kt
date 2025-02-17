package com.ivy.sdk.remote.config

import com.ivy.sdk.base.storage.AbsLocalStorage

class RCLocalStorage private constructor() : AbsLocalStorage() {

    companion object {
        val Instance by lazy(LazyThreadSafetyMode.NONE) {
            RCLocalStorage()
        }
    }


}