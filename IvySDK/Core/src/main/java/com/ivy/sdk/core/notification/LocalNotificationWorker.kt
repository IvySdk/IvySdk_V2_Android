package com.ivy.sdk.core.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ivy.sdk.base.utils.ILog

class LocalNotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {
        try {
            val data = inputData
            val title = data.getString("title") ?: ""
            val subtitle = data.getString("subtitle")
            val bigText = data.getString("bigText")
            val action = data.getString("action")
            val smallIcon = data.getString("smallIcon")
            val largeIcon = data.getString("largeIcon")
            val bigPicture = data.getString("bigPicture")
            val autoClose = data.getBoolean("bigPicture", true)

            var smallIconBitmap: Bitmap? = null
            var largeIconBitmap: Bitmap? = null
            var bigPictureBitmap: Bitmap? = null
            val assetManager = applicationContext.assets
            smallIcon?.let { picTitle ->
                try {
                    val input = assetManager.open(picTitle)
                    smallIconBitmap = BitmapFactory.decodeStream(input)
                    input.close()
                } catch (e: Exception) {
                    ILog.e("notification", "read small icon err:${e.message}")
                }
            }
            largeIcon?.let { picTitle ->
                try {
                    val input = assetManager.open(picTitle)
                    largeIconBitmap = BitmapFactory.decodeStream(input)
                    input.close()
                } catch (e: Exception) {
                    ILog.e("notification", "read small icon err:${e.message}")
                }
            }
            bigPicture?.let { picTitle ->
                try {
                    val input = assetManager.open(picTitle)
                    bigPictureBitmap = BitmapFactory.decodeStream(input)
                    input.close()
                } catch (e: Exception) {
                    ILog.e("notification", "read small icon err:${e.message}")
                }
            }

            LocalNotificationUtil.showNotification(
                applicationContext,
                title,
                subtitle,
                bigText,
                smallIconBitmap,
                largeIconBitmap,
                bigPictureBitmap,
                autoClose,
                action
            )
        } catch (e: Exception) {
            ILog.e("notification", "error to do notification task:${e.message}")
        }
        return Result.retry()
    }
}