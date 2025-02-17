package com.ivy.sdk.base.utils

import android.util.Log
import androidx.annotation.IntRange

class ILog {

    companion object {

        const val ITag: String = "ITAG"

        /**
         * level 对应系统log级别
         * Log.VERBOSE     2
         * Log.DEBUG       3
         * Log.INFO        4
         * Log.WARN        5
         * Log.ERROR       6
         * Log.ASSERT      7(no use)
         * Log.NONE        8
         *
         * @param level
         */
        @IntRange(from = 2, to = 8)
        var logLevel: Int = 2

        fun v(tag: String?, vararg msg: String) =
            assetLogLevel(Log.VERBOSE) ?: Log.v(formatTag(tag), formatMessage(*msg))

        fun d(tag: String?, vararg msg: String) =
            assetLogLevel(Log.DEBUG) ?: Log.d(formatTag(tag), formatMessage(*msg))

        fun i(tag: String?, vararg msg: String) =
            assetLogLevel(Log.INFO) ?: Log.i(formatTag(tag), formatMessage(*msg))

        fun w(tag: String?, vararg msg: String) =
            assetLogLevel(Log.WARN) ?: Log.w(formatTag(tag), formatMessage(*msg))

        fun e(tag: String?, vararg msg: String) =
            assetLogLevel(Log.ERROR) ?: Log.e(formatTag(tag), formatMessage(*msg))

        private fun formatTag(tag: String?): String = "${ITag}_${tag ?: ""}"

        private fun formatMessage(vararg msg: String): String = msg.joinToString(";")

        private fun assetLogLevel(cur: Int): Boolean? = if (logLevel <= cur) null else true

    }

}