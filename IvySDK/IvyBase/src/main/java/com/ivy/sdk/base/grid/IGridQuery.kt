package com.ivy.sdk.base.grid

interface IGridQuery {

    fun getGridConfig(key: String, defaultValue: String? = null): String?

    fun getGridConfig(key: String, defaultValue: Int = 0): Int

    fun getGridConfig(key: String, defaultValue: Double = 0.0): Double

    fun getGridConfig(key: String, defaultValue: Boolean = false): Boolean

    fun getGridConfig(key: String, defaultValue: Long = 0L): Long

}