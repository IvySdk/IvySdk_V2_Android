package com.ivy.sdk.ads

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.AdProvider
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdProvider
import com.ivy.sdk.base.utils.ILog
import org.json.JSONArray


internal interface IBaseAdHelper {
    fun getAdProvider(adProvider: String, autoInstantiate: Boolean = false): IAdProvider?
}

internal abstract class IvyBaseAd {

    protected var debug: Boolean = false
    protected var adConfig: AdConfig? = null
    protected var adListener: IAdListener? = null
    protected var helper: IBaseAdHelper? = null

    protected val ads: MutableList<AdProperty> = mutableListOf()

    fun setup(debug: Boolean, data: JSONArray, adConfig: AdConfig, helper: IBaseAdHelper, adListener: IAdListener) {
        this.debug = debug
        this.adConfig = adConfig
        this.helper = helper
        this.adListener = adListener
        try {
            val count = data.length()
            for (index in 0 until count) {
                val item = data.optJSONObject(index)
                val provider = item.optString("provider")
                val adProvider = helper.getAdProvider(provider, true)
                if (adProvider == null) {
                    ILog.e("", "unsupported provider:$provider !!!")
                    continue
                }

                val adPlatform = AdProvider.retrieve(provider)
                if (adPlatform == null) {
                    ILog.e("", "unsupported ad platform:$provider !!!")
                    continue
                }
                val adProperty = AdProperty.decode(getAdType(), adPlatform, item.optJSONObject("p"))
                if (adProperty == null) {
                    ILog.e("", "invalid ad config")
                    continue
                }
                ads.add(adProperty)
                adProvider.addTask(adProperty)
            }
            ads.sortBy { it.priority }
        } catch (_: Exception) {

        }
    }

    abstract fun getAdType(): AdType

    abstract fun isReady(): Boolean

    abstract fun showAd(activity: Activity, tag: String, placement: Int, clientInfo: String?)

    open fun getBannerAdView(adUnit: String): View? = null

    open fun showBannerAd(adUnit: String, container: FrameLayout, tag: String, placement: Int, clientInfo: String?): Boolean = false

    open fun closeBannerAd(adUnit: String, placement: Int) {}

    open fun isBannerAdReady(adUnit: String): Boolean = false

    open fun reloadBannerAd(adUnit: String?){}

    open fun getAdUnits(): MutableList<String> {
        val list: MutableList<String> = mutableListOf()
        for (adProperty in ads) {
            list.add(adProperty.adUnit)
        }
        return list
    }

    open fun onResume(container:FrameLayout?){}

    open fun onPause(container:FrameLayout?){}

    open fun onDestroy(){}

}