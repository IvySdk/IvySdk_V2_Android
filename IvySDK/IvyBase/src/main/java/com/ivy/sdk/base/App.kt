package com.ivy.sdk.base

import android.annotation.SuppressLint

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import com.ivy.sdk.base.storage.GameDataStorage
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.IKeys
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class App : Application(){


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var Instance: App

        const val ID_MMKV: String = "ev";
        const val ID_MMKV_GAMEDATA = "_gamedata_"

    }

    private fun getLauncherProcessName(c: Context, pid: Int): String? {
        val activityManager = c.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfoList = activityManager.runningAppProcesses
        if (processInfoList != null) {
            for (info in processInfoList) {
                if (info.pid == pid) {
                    return info.processName
                }
            }
        }
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val launcherProcessName = getProcessName()
            if (launcherProcessName != packageName) {
                ILog.i("App", "stop call application by $launcherProcessName")
                //  return
            } else {
                start()
            }
        } else {
            val pid = android.os.Process.myPid()
            val launcherProcessName = getLauncherProcessName(this, pid)
            if (launcherProcessName != null && launcherProcessName == packageName) {
                start()
            } else {
                ILog.i("App", "stop call application by $launcherProcessName")
                //    return
            }
        }
    }

    private fun start() {
        ILog.i("App", "application start initialize")
        Instance = this
        LocalStorage.Instance.initLocalStorage(this, id = ID_MMKV)
        GameDataStorage.Instance.initLocalStorage(this, id = ID_MMKV_GAMEDATA)
        //判断用户是否已设置role_id,如未设置，则尝试获取adId作为用户role_id
        if (Util.isRoleIdAlreadySet()) {
            ILog.i("App", "role id already set! start init config")
            //     GridManager.Instance.setup()
            setupAppConfig()
        } else {
            //尝试读取 adId ,耗时操作，
            ILog.i("App", "role id had not set! try load adId")
            CoroutineScope(Dispatchers.Default).launch {
                suspendCoroutine<String?> { condition ->
                    Util.getAdvertisingId { adId ->
                        adId?.let {
                            LocalStorage.Instance.encodeString(IKeys.KEY_GOOGLE_ADVERTISING_ID, adId)
                        }
                        ILog.e("App", "got adId:$adId, start init sdk config")
                        condition.resume(adId)
                    }
                }
                launch (Dispatchers.Main){
                    setupAppConfig()
                }
            }
        }
    }

    open fun setupAppConfig() {}


}
