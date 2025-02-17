package com.ivy.sdk.base.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Base64
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.ivy.sdk.base.App
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.IKeys
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object Util {

        val ivBytes: ByteArray = byteArrayOf(
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30,
            0x30
        )

        private var _keyHash: String? = null

        fun screenWith(): Int {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics =
                        (App.Instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
                    return windowMetrics.bounds.width()
                } else {
                    val displayMetrics = DisplayMetrics()
                    val windowManager =
                        (App.Instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager)

                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    return displayMetrics.widthPixels
                }
            } catch (e: Exception) {
                ILog.i("Util", "get screen with err:${e.message}")
            }
            return 0
        }

        fun screenHeight(): Int {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics =
                        (App.Instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
                    return windowMetrics.bounds.width()
                } else {
                    val displayMetrics = DisplayMetrics()
                    val windowManager =
                        (App.Instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    return displayMetrics.heightPixels
                }
            } catch (e: Exception) {
                ILog.i("Util", "get screen height err:${e.message}")
            }
            return 0
        }

        fun versionCode(): Long {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    return App.Instance.packageManager.getPackageInfo(
                        App.Instance.packageName,
                        PackageInfo.INSTALL_LOCATION_AUTO
                    ).longVersionCode
                }
                return App.Instance.packageManager.getPackageInfo(
                    App.Instance.packageName,
                    PackageInfo.INSTALL_LOCATION_AUTO
                ).versionCode.toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 0L
        }

        fun versionName(): String {
            try {
                return App.Instance.packageManager.getPackageInfo(
                    App.Instance.packageName,
                    PackageInfo.INSTALL_LOCATION_AUTO
                ).versionName
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun packageName(): String = App.Instance.packageName

        /**
         * 优先 adId作为用户唯一id，如果获取adId失败，则自定义生成uuid
         */
        fun roleId(): String {
            var uuid = LocalStorage.Instance.decodeString("_ANDROID_*****_UUID_", null)
            if (uuid.isNullOrEmpty()) {
                uuid = LocalStorage.Instance.decodeString(IKeys.KEY_GOOGLE_ADVERTISING_ID)
                if (uuid.isNullOrEmpty()) {
                    uuid = UUID.randomUUID().toString()
                }
                LocalStorage.Instance.encodeString("_ANDROID_*****_UUID_", uuid)
            }
            return uuid
        }

        fun isRoleIdAlreadySet(): Boolean {
            val uuid = LocalStorage.Instance.decodeString("_ANDROID_*****_UUID_", null)
            return !uuid.isNullOrEmpty()
        }

        @OptIn(DelicateCoroutinesApi::class)
        fun getAdvertisingId(callback: (String?) -> Unit) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    var adId: String? = null
                    try {
                        val info = AdvertisingIdClient.getAdvertisingIdInfo(App.Instance)
                        // val state = info.isLimitAdTrackingEnabled
                        adId = info.id
                        if (adId.isNullOrEmpty()) {
                            adId = "00000000-0000-0000-0000-000000000000"
                        }
                        if (adId.startsWith("00000000")) {
                            adId = null
                        }
                    } catch (e: Exception) {
                        ILog.e("", "load adId failed:${e.message}")
                    }
                    withContext(Dispatchers.Main) {
                        callback.invoke(adId)
                    }
                }
            }
        }

        fun md5(content: String): String {
            try {
                val bytes = MessageDigest.getInstance("md5").digest(content.toByteArray())
                val ret = StringBuilder(bytes.size shl 1)
                for (aByte in bytes) {
                    ret.append(Character.forDigit((aByte.toInt() shr 4) and 0xf, 16))
                    ret.append(Character.forDigit(aByte.toInt() and 0xf, 16))
                }
                return ret.toString()
            } catch (exp: java.lang.Exception) {
                return content
            }
        }

        private fun getKeyStoreHash(context: Context): String? {
            try {
                val info = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    return Base64.encodeToString(md.digest(), Base64.DEFAULT).trim { it <= ' ' }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }

        private fun getKeyHash(context: Context): String? {
            if (_keyHash == null) {
                _keyHash = getKeyStoreHash(context)
                if (_keyHash != null && _keyHash!!.length < 32) {
                    val padding = 32 - _keyHash!!.length
                    val s = ByteArray(padding)
                    System.arraycopy(ivBytes, 0, s, 0, padding)
                    _keyHash += String(s)
                }
            }
            return _keyHash
        }

        fun encodeParams(context: Context, data: String): String? {
            return encodeParams(context, data.toByteArray())
        }

        private fun encodeParams(context: Context, data: ByteArray?): String? {
            try {
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    SecretKeySpec(getKeyHash(context)!!.toByteArray(), "AES"),
                    IvParameterSpec(ivBytes)
                )
                val plainText = cipher.doFinal(data)
                val result = Base64.encodeToString(plainText, Base64.URL_SAFE or Base64.NO_WRAP)
                    .trim { it <= ' ' }

                ILog.i("Util", "encodeParams result: >>> $result")
                return result
            } catch (e: Throwable) {
                e.printStackTrace()
                return null
            }
        }

        fun decodeParams(context: Context, data: String): String? {
            return decodeParams(context, data.toByteArray(charset = Charsets.UTF_8))
        }

        fun decodeWithKey(content: String?, key: String): String? {
            try {
                val decode = Base64.decode(content, Base64.URL_SAFE or Base64.NO_WRAP)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(key.toByteArray(), "AES"),
                    IvParameterSpec(ivBytes)
                )
                val plainText = cipher.doFinal(decode)
                return (String(plainText))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                return null
            }
        }

        private fun decodeParams(context: Context, data: ByteArray?): String? {
            try {
                val keyHash = getKeyHash(context)!!.toByteArray(Charsets.UTF_8)
                val decode = Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP)
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    SecretKeySpec(keyHash, "AES"),
                    IvParameterSpec(ivBytes)
                )
                val plainText = cipher.doFinal(decode)
                return (String(plainText))
            } catch (e: Throwable) {
                e.printStackTrace()
                return null
            }
        }

        fun base64encode(input: String): String {
            return Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        }

        fun base64Decode(input: String): String {
            return String(Base64.decode(input, Base64.DEFAULT))
        }

        fun dp2Px(context: Context, dp: Float): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi / 160f)
        }

        fun getTotalMemory(): Long {
            try {
                val activity: Activity = ActivityUtil.Instance.activity ?: return -1
                val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val info = ActivityManager.MemoryInfo()
                manager.getMemoryInfo(info)
                return info.totalMem
            } catch (_: Throwable) {

            }
            return -1L
        }

        fun getFreeMemory(): Long {
            try {
                val activity: Activity = ActivityUtil.Instance.activity ?: return -1
                val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val info = ActivityManager.MemoryInfo()
                manager.getMemoryInfo(info)
                return info.availMem
            } catch (_: Throwable) {

            }
            return -1L
        }

        fun getTotalDiskSize(): Long {
            try {
                val internalStorageDir = Environment.getDataDirectory()
                val statFs = StatFs(internalStorageDir.path)
                val availableBytes: Long = statFs.totalBytes
                //long availableMB = availableBytes / (1024 * 1024);
                return availableBytes
            } catch (_: Exception) {
            }
            return -1L
        }

        fun getAvailableDiskSize(): Long {
            try {
                val internalStorageDir = Environment.getDataDirectory()
                val statFs = StatFs(internalStorageDir.path)
                val availableBytes: Long = statFs.availableBytes
                //long availableMB = availableBytes / (1024 * 1024);
                return availableBytes
            } catch (_: Exception) {
            }
            return -1L
        }

        fun byte2KB(bytes: Long): Int = (bytes / 1024.0f).toInt()

        fun byte2MB(bytes: Long): Int = (bytes / 1024.0f / 1024.0f).toInt()

        fun copyTxt(txt: String): Boolean {
            try {
                val clipBoard = App.Instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", txt)
                clipBoard.setPrimaryClip(clipData)
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

    fun isNetworkConnected(): Boolean {
        try {
            val connectivityManager = App.Instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        } catch (e: Exception) {

        }
        return false
    }


}