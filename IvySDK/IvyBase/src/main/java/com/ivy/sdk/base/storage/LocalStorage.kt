package com.ivy.sdk.base.storage

import android.app.Application
import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * local storage
 */
class LocalStorage private constructor() : AbsLocalStorage() {


    companion object {
        val Instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            LocalStorage()
        }
    }

    override fun initLocalStorage(
        application: Application,
        id: String?,
        rootDir: String?,
        multiProcessSupport: Boolean,
        cryptKey: String?
    ) {
        super.initLocalStorage(application, id, rootDir, multiProcessSupport, cryptKey)
        //TODO: 旧版本 SharedPreferences 及 MMKV 数据 需要转移
        importFromSharedPreferences(
            context = application,
            title = "_cyj_promotion",
            mode = Context.MODE_PRIVATE
        )
        importFromSharedPreferences(
            context = application,
            title = "pref",
            mode = Context.MODE_PRIVATE
        )
        importFromSharedPreferences(
            context = application,
            title = "pays",
            mode = Context.MODE_PRIVATE
        )
    }

    private fun importFromSharedPreferences(
        context: Context,
        title: String,
        mode: Int = Context.MODE_PRIVATE
    ) {
        val sharedPreferences = context.getSharedPreferences(title, mode)
        if (!sharedPreferences.all.isNullOrEmpty()) {
            mmkv?.importFromSharedPreferences(sharedPreferences)
            sharedPreferences.edit().clear().apply()
        }
    }


}