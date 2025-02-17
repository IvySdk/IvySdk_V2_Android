package com.ivy.sdk.base.storage

import android.app.Application
import android.os.Parcelable

interface ILocalStorage {

    fun initLocalStorage(
        application: Application,
        id: String? = null,
        rootDir: String? = null,
        multiProcessSupport: Boolean = false,
        cryptKey: String? = null
    )

    fun isReadOnly(): Boolean

    fun isMultiProcessSupport(): Boolean

    fun contains(key: String): Boolean

    fun containsKey(key: String): Boolean

    fun removeValueForKey(key: String)

    fun removeValuesForKey(key: Array<String>)

    fun allKeys(): Array<String>?

    fun allKeysNotExpired(): Array<String>?

    fun clearAll()

    fun encodeInt(key: String, value: Int, expireDurationInSecond: Int = -1)

    fun decodeInt(key: String, defaultValue: Int? = null): Int

    fun encodeLong(key: String, value: Long, expireDurationInSecond: Int = -1)

    fun decodeLong(key: String, defaultValue: Long? = null): Long

    fun encodeString(key: String, value: String, expireDurationInSecond: Int = -1)

    fun decodeString(key: String, defaultValue: String? = null): String?

    fun encodeBoolean(key: String, value: Boolean, expireDurationInSecond: Int = -1)

    fun decodeBoolean(key: String, defaultValue: Boolean? = null): Boolean

    fun encodeFloat(key: String, value: Float, expireDurationInSecond: Int = -1)

    fun decodeFloat(key: String, defaultValue: Float? = null): Float

    fun encodeDouble(key: String, value: Double, expireDurationInSecond: Int = -1)

    fun decodeDouble(key: String, defaultValue: Double? = null): Double

    fun encodeBytes(key: String, value: ByteArray, expireDurationInSecond: Int = -1)

    fun decodeBytes(key: String, defaultValue: ByteArray? = null): ByteArray?

    fun encodeParcelable(key: String, value: Parcelable, expireDurationInSecond: Int = -1)

    fun <T : Parcelable> decodeParcelable(key: String, tClass: Class<T>, defaultValue: T?): T?

    fun encodeStringSet(key: String, value: Set<String>, expireDurationInSecond: Int = -1)

    fun decodeStringSet(key: String, defaultValue: Set<String>?): Set<String>?

}