package com.ivy.sdk.base.grid

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import org.json.JSONObject

/**
 *  配置文件 default.json 读取、同步
 *  下发配置再重启时生效， 刷新时间：60分钟
 */
class GridManager private constructor() : IGridQuery {

    companion object {
        const val TAG = "GridManager"
        val Instance: GridManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { GridManager() }
    }

    private var liveGrid: MutableLiveData<JSONObject?> = MutableLiveData<JSONObject?>(null)

    abstract class IGrid {
        //统计
        open fun onTrackConfig(data: String?) {}

        //广告
        open fun onAdConfig(data: String?) {}

        //客服
        open fun onCustomerServiceConfig(data: String?) {}

        //推送
        open fun onPushConfig(data: String?) {}

        //计费
        open fun onPayment(data: String?) {}

        //自有 remote config
        open fun onGridRemoteConfig(data: String?) {}

        //游戏服务
        open fun onGameServices(data: String?) {}
        //其它，自取
        open fun onData() {}
    }

    fun setData(json: JSONObject) {
        liveGrid.value = json
    }

    fun addListener(observer: Observer<JSONObject?>) {
        liveGrid.observeForever(observer)
    }

    fun removeListener(observer: Observer<JSONObject?>) {
        liveGrid.removeObserver(observer)
    }

    fun parseConfig(callback: IGrid) {
        val gridData = liveGrid.value
        //统计
        callback.onTrackConfig(gridData?.optString("track"))
        //自定义远程配置
        callback.onGridRemoteConfig(gridData?.optString("remoteConfig"))
        //客服
        callback.onCustomerServiceConfig(gridData?.optString("helper"))
        //推送
        callback.onPushConfig(gridData?.optString("push"))
        //广告
        callback.onAdConfig(gridData?.optString("ads"))
        //计费
        callback.onPayment(gridData?.optString("payment"))
        //基础服务
        callback.onGameServices(gridData?.optString("gameServices"))
        //其它
        callback.onData()
    }

    /**
     * 获取第一层数据值
     */
    override fun getGridConfig(key: String, defaultValue: String?): String? =
        liveGrid.value?.optString(key, defaultValue) ?: defaultValue

    override fun getGridConfig(key: String, defaultValue: Int): Int =
        liveGrid.value?.optInt(key, defaultValue) ?: defaultValue

    override fun getGridConfig(key: String, defaultValue: Double): Double =
        liveGrid.value?.optDouble(key, defaultValue) ?: defaultValue

    override fun getGridConfig(key: String, defaultValue: Boolean): Boolean =
        liveGrid.value?.optBoolean(key, defaultValue) ?: defaultValue

    override fun getGridConfig(key: String, defaultValue: Long): Long =
        liveGrid.value?.optLong(key, defaultValue) ?: defaultValue


}