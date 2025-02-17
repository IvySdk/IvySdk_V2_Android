package com.ivy.sdk.core.notification

class NotificationUtil {

    companion object {

        const val TAG = "Notification"

        fun isPermissionEnabled(): Boolean = PermissionUtil.isPermissionEnabled()

        /**
         * 判断当前权限状态
         * 0: 权限被彻底拒绝，需要跳转设置页面开启
         * 1: 权限已开启
         * 2: 权限状态待定，仍可通过系统接口请求
         */
        fun loadNotificationPermissionState(): Int = PermissionUtil.loadNotificationPermissionState()

        /**
         * 仅弹出系统权限请求弹窗
         */
        fun requestNotificationPermission() {
            PermissionUtil.requestNotificationPermission()
        }

        fun autoRequestNotificationPermission() {
            PermissionUtil.autoRequestNotificationPermission()
        }

        fun openNotificationSettings() {
            PermissionUtil.openNotificationSettings()
        }

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

        fun isNotificationChannelClosed(channelId: String): Boolean = PermissionUtil.isNotificationChannelClosed(channelId)

        fun openNotificationChannel(channelId: String) {
            PermissionUtil.openNotificationChannel(channelId)
        }

        fun addNotificationTask(
            tag: String,
            title: String,
            subtitle: String?,
            bigText: String?,
            smallIcon: String?,
            largeIcon: String?,
            bigPicture: String?,
            delay: Long,
            autoCancel: Boolean,
            action: String?,
            repeat: Boolean,
            requireNetwork: Boolean,
            requireCharging: Boolean
        ) = LocalNotificationUtil.addNotificationTask(
            tag,
            title,
            subtitle,
            bigText,
            smallIcon,
            largeIcon,
            bigPicture,
            delay,
            autoCancel,
            action,
            repeat,
            requireNetwork,
            requireCharging
        )

        fun cancelTask(tag: String) = LocalNotificationUtil.cancelTask(tag)

        fun cancelAllTask() = LocalNotificationUtil.cancelAllTask()

    }

}