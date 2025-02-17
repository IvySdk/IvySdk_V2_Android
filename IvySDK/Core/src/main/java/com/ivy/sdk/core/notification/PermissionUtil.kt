package com.ivy.sdk.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ivy.sdk.base.App
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog

class PermissionUtil {

    companion object {

        private const val NOTIFICATION_REQUEST_CODE = 101;

        fun isPermissionEnabled(): Boolean {
            return ActivityUtil.Instance.activity?.let { activity ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return@let ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    NotificationManagerCompat.from(activity).areNotificationsEnabled()
                }
                false
            } ?: run {
                ILog.w(NotificationUtil.TAG, "invalid activity for check")
                false
            }
        }

        /**
         * 判断当前权限状态
         * 0: 权限被彻底拒绝，需要跳转设置页面开启
         * 1: 权限已开启
         * 2: 权限状态待定，仍可通过系统接口请求
         */
        fun loadNotificationPermissionState(): Int {
            return ActivityUtil.Instance.activity?.let { activity ->
                return@let when (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        ILog.i(NotificationUtil.TAG, "notification permission granted")
                        1
                    }

                    else -> {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                            ILog.i(NotificationUtil.TAG, "should request permission by client")
                            return@let 2
                        } else {
                            val isFirstRequest: Boolean = LocalStorage.Instance.decodeBoolean("is_first_notification", false)
                            if (!isFirstRequest) {
                                ILog.i(NotificationUtil.TAG, "first request! just require by client")
                                return@let 2
                            }
                            return@let 0
                        }
                    }
                }
            } ?: run {
                ILog.w(NotificationUtil.TAG, "invalid activity for check")
                1
            }
        }

        /**
         * 仅弹出系统权限请求弹窗
         */
        fun requestNotificationPermission() {
            ActivityUtil.Instance.activity?.let { activity ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查权限是否已被授予
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // 用户未授予权限，检查是否可以请求权限
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_REQUEST_CODE
                        )
                    } else {
                        // 权限已被授予
                        ILog.i(NotificationUtil.TAG, "permission already allowed")
                    }
                } else {
                    // Android 6.0 以下版本，权限自动授予
                    ILog.i(NotificationUtil.TAG, "permission auto allowed on system lower 23")
                }
            }
        }

        fun autoRequestNotificationPermission() {
            ActivityUtil.Instance.activity?.let { activity ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // 检查权限是否已被授予
                        if (ActivityCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // 用户未授予权限，检查是否可以请求权限
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
                                // 可以请求权限，显示解释
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                                    NOTIFICATION_REQUEST_CODE
                                )
                            } else {
                                // 用户选择了“不要再询问”或是初次请求
                                val state: Boolean = LocalStorage.Instance.decodeBoolean("is_first_notification", true)
                                if (state) {
                                    //初次请求，默认可使用系统接口申请权限
                                    ActivityCompat.requestPermissions(
                                        activity,
                                        arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                                        NOTIFICATION_REQUEST_CODE
                                    )
                                } else {
                                    openNotificationSettings()
                                }
                            }
                        } else {
                            // 权限已被授予
                        }
                    } else {
                        val enabled = NotificationManagerCompat.from(activity).areNotificationsEnabled()
                        if (!enabled) {
                            openNotificationSettings()
                        }
                    }
                } else {
                    // Android 6.0 以下版本，权限自动授予
                }
            }
        }

        fun openNotificationSettings() {
            try {
                ActivityUtil.Instance.activity?.let { activity ->
                    val intent = Intent()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
                        intent.putExtra("app_package", activity.packageName)
                        intent.putExtra("app_uid", activity.applicationInfo.uid)
                    } else {
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.setData(Uri.parse("package:" + activity.packageName))
                    }
                    activity.startActivity(intent)
                } ?: ILog.i(NotificationUtil.TAG, "open permission settings error: activity invalid")
            } catch (t: Throwable) {
                ILog.e(NotificationUtil.TAG, "open permission settings error:${t.message}")
            }
        }

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            try {
                ActivityUtil.Instance.activity?.let { activity ->
                    if (requestCode == NOTIFICATION_REQUEST_CODE && permissions.isNotEmpty()) {
                        for (permission in permissions) {
                            if (permission == Manifest.permission.POST_NOTIFICATIONS) {
                                val state =
                                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
                                val preState: Boolean = LocalStorage.Instance.decodeBoolean("is_first_notification", false)
                                if (state && !preState) {
                                    LocalStorage.Instance.encodeBoolean("is_first_notification", true)
                                }
                            }
                        }
                    }
                } ?: ILog.i(NotificationUtil.TAG, "parse permission request result error: activity invalid!!!")
            } catch (e: Exception) {
                ILog.e(NotificationUtil.TAG, "parse permission request result error:${e.message}")
            }
        }

        fun isNotificationChannelClosed(channelId: String): Boolean {
            try {
                var channel: NotificationChannel? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    channel = (App.Instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).getNotificationChannel(channelId)
                    if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                        return true
                    }
                }
            } catch (e: Exception) {
                ILog.e(NotificationUtil.TAG, "check channel state err:${e.message}")
            }
            return false
        }

        fun openNotificationChannel(channelId: String) {
            try {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, App.Instance.packageName)
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                ActivityUtil.Instance.activity?.startActivity(intent)
            } catch (e: Exception) {
                ILog.e(NotificationUtil.TAG, "require open channel err:${e.message}")
            }
        }


    }

}