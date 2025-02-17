package com.ivy.sdk.base.storage

import android.app.Application
import android.os.Parcelable
import com.ivy.sdk.base.utils.ILog
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import kotlin.reflect.KClass

abstract class AbsLocalStorage : ILocalStorage {

    companion object {
        const val TAG: String = "LocalStorage"
    }

    protected var mmkv: MMKV? = null

    override fun initLocalStorage(
        application: Application,
        id: String?,
        rootDir: String?,
        multiProcessSupport: Boolean,
        cryptKey: String?
    ) {
        val initResult: String =
            rootDir?.let {
                return@let MMKV.initialize(application, it, MMKVLogLevel.LevelWarning)
            } ?: MMKV.initialize(application, MMKVLogLevel.LevelWarning)
        ILog.i(TAG, "MMKV init result:${initResult}")
        mmkv = MMKV.mmkvWithID(
            id ?: "default_ev", when (multiProcessSupport) {
                true -> MMKV.MULTI_PROCESS_MODE
                false -> MMKV.SINGLE_PROCESS_MODE
            }, cryptKey
        )
    }

    override fun isReadOnly(): Boolean {
        return false
    }

    override fun isMultiProcessSupport(): Boolean {
        return false
    }

    override fun contains(key: String): Boolean {
        return mmkv?.contains(key) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            false
        }
    }

    override fun containsKey(key: String): Boolean {
        return mmkv?.containsKey(key) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            false
        }
    }

    override fun removeValueForKey(key: String) {
        mmkv?.removeValueForKey(key) ?: ILog.e(TAG, "MMKV not initialized !!!")
    }

    override fun removeValuesForKey(key: Array<String>) {
        mmkv?.removeValuesForKeys(key) ?: ILog.e(TAG, "MMKV not initialized !!!")
    }

    override fun allKeys(): Array<String>? {
        return mmkv?.allKeys() ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }

    override fun allKeysNotExpired(): Array<String>? {
        return mmkv?.allNonExpireKeys() ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }

    override fun clearAll() {
        mmkv?.clearAll() ?: ILog.e(TAG, "MMKV not initialized !!!")
    }

    override fun encodeInt(key: String, value: Int, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeInt(key: String, defaultValue: Int?): Int {
        return mmkv?.decodeInt(key, defaultValue ?: 0) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            0
        }
    }

    override fun encodeLong(key: String, value: Long, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeLong(key: String, defaultValue: Long?): Long {
        return mmkv?.decodeLong(key, defaultValue ?: 0L) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            0L
        }
    }

    override fun encodeString(key: String, value: String, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeString(key: String, defaultValue: String?): String? {
        return mmkv?.decodeString(key, defaultValue ?: "") ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }

    override fun encodeBoolean(key: String, value: Boolean, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeBoolean(key: String, defaultValue: Boolean?): Boolean {
        return mmkv?.decodeBool(key, defaultValue ?: false) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            false
        }
    }

    override fun encodeFloat(key: String, value: Float, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeFloat(key: String, defaultValue: Float?): Float {
        return mmkv?.decodeFloat(key, defaultValue ?: 0.0f) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            0.0f
        }
    }

    override fun encodeDouble(key: String, value: Double, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeDouble(key: String, defaultValue: Double?): Double {
        return mmkv?.decodeDouble(key, defaultValue ?: 0.0) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            0.0
        }
    }

    override fun encodeBytes(key: String, value: ByteArray, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeBytes(key: String, defaultValue: ByteArray?): ByteArray? {
        return mmkv?.decodeBytes(key, defaultValue) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }

    override fun encodeParcelable(key: String, value: Parcelable, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun <T : Parcelable> decodeParcelable(
        key: String,
        tClass: Class<T>,
        defaultValue: T?
    ): T? {
        return mmkv?.decodeParcelable(key, tClass, defaultValue) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }

    override fun encodeStringSet(key: String, value: Set<String>, expireDurationInSecond: Int) {
        when (expireDurationInSecond > 0) {
            true -> mmkv?.encode(key, value, expireDurationInSecond) ?: ILog.e(
                TAG,
                "MMKV not initialized !!!"
            )

            false -> mmkv?.encode(key, value) ?: ILog.e(TAG, "MMKV not initialized !!!")
        }
    }

    override fun decodeStringSet(key: String, defaultValue: Set<String>?): Set<String>? {
        return mmkv?.decodeStringSet(key, defaultValue) ?: run {
            ILog.e(TAG, "MMKV not initialized !!!")
            null
        }
    }


}