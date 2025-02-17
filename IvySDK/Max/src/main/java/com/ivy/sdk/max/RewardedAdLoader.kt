package com.ivy.sdk.max

import android.app.Activity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxNetworkResponseInfo
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.AdConfig
import com.ivy.sdk.base.ads.AdProperty
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.ads.IPAMManager
import com.ivy.sdk.base.track.EventParams

internal class RewardedAdLoader(adProperty: AdProperty, adConfig: AdConfig, pamManager: IPAMManager?, adListener: IAdListener) :
    IAdLoader(adProperty, adConfig, pamManager, adListener),
    MaxRewardedAdListener, MaxAdRevenueListener {

    private var rewardedAd: MaxRewardedAd? = null
    private var gotReward: Boolean = false

    override fun isReady(): Boolean = rewardedAd != null && (rewardedAd?.isReady ?: false)

    override fun doLoadAd() {
        try {
            rewardedAd = MaxRewardedAd.getInstance(adProperty.adUnit, App.Instance)
            rewardedAd?.setListener(this)
            rewardedAd?.setRevenueListener(this)
            rewardedAd?.loadAd()
        } catch (e: Exception) {
            onAdLoadFailure("${e.message}")
        }
    }

    override fun show(activity: Activity, tag: String, placement: Int, clientInfo: String?) {
        super.show(activity, tag, placement, clientInfo)
        rewardedAd?.showAd(activity) ?: onAdShowFailed("invalid ad")
    }

    override fun onAdLoaded(p0: MaxAd) {
        try {
            val waterfall = p0.waterfall.networkResponses
            var mediationNetWork: String? = null
            for (it in waterfall) {
                if (it.adLoadState == MaxNetworkResponseInfo.AdLoadState.AD_LOADED) {
                    mediationNetWork = it.mediatedNetwork.name
                    break
                }
            }
            mediationNetWork?.let { eventParams[EventParams.EVENT_PARAM_AD_MEDIATION] = it }
        } catch (_: Exception) {
        }
        onAdLoadSuccess()
    }

    override fun onAdDisplayed(p0: MaxAd) {
        onAdShowSuccess()
    }

    override fun onAdHidden(p0: MaxAd) {
        onAdClosed(gotReward)
    }

    override fun onAdClicked(p0: MaxAd) {
        onAdClicked()
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        onAdLoadFailure("$p0;${p1.code};${p1.message}")
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        onAdShowFailed("$p0;${p1.code};${p1.message}")
    }

    override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
        gotReward = true
       // this@RewardedAdLoader.onAdClosed(gotReward, taskId)
        onUserRewarded()
    }

    override fun onAdRevenuePaid(ad: MaxAd) {
        try {
            val revenue = ad.revenue
            if (revenue >= 10.0 || revenue <= 0) {
                return
            }
            onAdRevenuePaid(revenue, "USD")
        } catch (_:Exception){}
    }


}