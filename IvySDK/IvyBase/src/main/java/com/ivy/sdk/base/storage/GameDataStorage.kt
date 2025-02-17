package com.ivy.sdk.base.storage

import android.app.Application

class GameDataStorage private constructor() : AbsLocalStorage() {

    companion object {
        val Instance by lazy(LazyThreadSafetyMode.NONE) {
            GameDataStorage()
        }
    }


}