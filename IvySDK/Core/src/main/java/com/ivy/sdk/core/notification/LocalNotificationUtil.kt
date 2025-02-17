package com.ivy.sdk.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ivy.sdk.base.App
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.core.R
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class LocalNotificationUtil {

    companion object {

        const val DEFAULT_CHANNEL_ID = "game_messages"
        const val DEFAULT_CHANNEL_NAME = "Game Messages"

        /**
         * 添加本地通知任务
         * 所有图片配置方式：  图片名称为全名，携带后缀， 放置在 asset 目录下；
         *  请不要使用过大尺寸的图片
         *  smallIcon: 24*24   largeIcon: 144*144    bigPicture:最大高度为 256dp
         *
         *
         * @param tag               任务唯一标识
         * @param title             通知 标题
         * @param subtitle          通知子标题
         * @param bigText           长文本，和大图 二选一 , 优先大图
         * @param smallIcon
         * @param largeIcon
         * @param bigPicture        大图，和长文本 二选一, 优先大图
         * @param delay             延迟展示时长
         * @param autoCancel        通知是否可自动关闭
         * @param action            通知行为
         * @param repeat            重复展示
         * @param requireNetwork    必须为联网状态触发
         * @param requireCharging   必须为充电状态触发
         */
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
        ) {
            if (repeat) {
                addPeriodTask(
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
                    requireNetwork,
                    requireCharging
                )
            } else {
                addOneTimeTask(
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
                    requireNetwork,
                    requireCharging
                )
            }
        }

        private fun addPeriodTask(
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
            requireNetwork: Boolean,
            requireCharging: Boolean
        ) {
            val data = buildTaskParams(title, subtitle, bigText, smallIcon, largeIcon, bigPicture, autoCancel, action)
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(if (requireNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                .setRequiresCharging(requireCharging)
                .build()
            val oneTimeWorkRequest: PeriodicWorkRequest = PeriodicWorkRequest.Builder(LocalNotificationWorker::class.java, delay, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES) //任务重试间隔
                .addTag(tag)
                .build()
            WorkManager.getInstance(App.Instance).enqueue(oneTimeWorkRequest)
        }

        private fun addOneTimeTask(
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
            requireNetwork: Boolean,
            requireCharging: Boolean
        ) {
            val data = buildTaskParams(title, subtitle, bigText, smallIcon, largeIcon, bigPicture, autoCancel, action)
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(if (requireNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                .setRequiresCharging(requireCharging)
                .build()
            val oneTimeWorkRequest: OneTimeWorkRequest = OneTimeWorkRequest.Builder(LocalNotificationWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES) //任务重试间隔
                .addTag(tag)
                .build()
            WorkManager.getInstance(App.Instance).enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, oneTimeWorkRequest)
        }

        private fun buildTaskParams(
            title: String,
            subtitle: String?,
            bigText: String?,
            smallIcon: String?,
            largeIcon: String?,
            bigPicture: String?,
            autoCancel: Boolean,
            action: String?
        ): Data = Data.Builder().apply {
            putString("title", title)
            subtitle?.let { putString("subtitle", it) }
            bigText?.let { putString("bigText", it) }
            smallIcon?.let { putString("smallIcon", it) }
            largeIcon?.let { putString("largeIcon", it) }
            bigPicture?.let { putString("bigPicture", it) }
            action?.let { putString("action", it) }
            putBoolean("autoClose", autoCancel)
        }.build()

        /**
         * 关闭对应tag的任务，无法确保一定会关闭
         *
         * @param context
         * @param tag
         */
        fun cancelTask(tag: String) {
            WorkManager.getInstance(App.Instance).cancelAllWorkByTag(tag)
        }

        /**
         * 关闭所有任务
         *
         * @param context
         */
        fun cancelAllTask() {
            WorkManager.getInstance(App.Instance).cancelAllWork()
        }

        fun showNotification(
            context: Context,
            title: String,
            subtitle: String?,
            bigText: String?,
            smallIcon: Bitmap?,
            largeIcon: Bitmap?,
            bigPicture: Bitmap?,
            autoCancel: Boolean,
            action: String?
        ): Boolean {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                    manager.createNotificationChannel(channel)
                }
                val intent: Intent? = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    ?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                action?.let { intent?.putExtra("src_action", it) }
                intent?.setPackage(context.packageName)

                val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(subtitle)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(autoCancel)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLargeIcon(largeIcon)
                smallIcon?.let {
                    builder.setSmallIcon(IconCompat.createWithBitmap(it))
                } ?: builder.setSmallIcon(R.drawable.icon_notification)
                bigPicture?.let {
                    builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(it).bigLargeIcon(largeIcon))
                } ?: {
                    bigText?.let {
                        builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText).setSummaryText(subtitle))
                    }
                }
                val itemId = Random.nextInt() % 100
                manager.notify(itemId, builder.build())
            } catch (e: Exception) {
                ILog.e(NotificationUtil.TAG, "show notification err:${e.message}")
            }
            return false
        }


    }

}