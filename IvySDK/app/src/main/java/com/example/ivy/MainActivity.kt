package com.example.ivy

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.client.AndroidSdk
import com.ivy.sdk.base.ads.AdType
import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.billing.IPurchaseResult
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.helper.IHelperCallback
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.IToast
import com.ivy.sdk.core.Builder
import com.ivy.sdk.core.IvySdk
import org.json.JSONObject
import kotlin.math.abs
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
companion object{
    const val TAG = "main"
}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = Builder.Build().setAdListener(object : IAdListener() {
            override fun onAdLoadSuccess(adType: AdType) {

            }

            override fun onAdLoadFailure(adType: AdType, reason: String?) {

            }

            override fun onAdShowSuccess(adType: AdType, tag: String, placement: Int) {
                ILog.i(TAG, "ad show success:${adType.value};$tag;$placement")
            }

            override fun onAdShowFailed(adType: AdType, reason: String?, tag: String, placement: Int) {
                ILog.i(TAG, "ad show failed:${adType.value};$tag;$placement")
            }

            override fun onAdClicked(adType: AdType, tag: String, placement: Int) {
                ILog.i(TAG, "ad clicked:${adType.value};$tag;$placement")
            }

            override fun onAdClosed(adType: AdType, gotReward: Boolean, tag: String, placement: Int) {
                ILog.i(TAG, "ad closed:${adType.value};$gotReward;$tag;$placement")
            }

        }).setPurchaseListener(object : IPurchaseResult {

            override fun payResult(payId: Int, status: Int, payload: String?, merchantTransactionId: String?) {
                AndroidSdk.toast("pay result:$payId; result:$status;merchantTransactionId:$merchantTransactionId")
                if (status == IPurchaseResult.PAY_SUCCEED) {
                    merchantTransactionId?.let {
                        AndroidSdk.shippingGoods(merchantTransactionId)
                    } ?: run {
                        ILog.e(TAG, "invalid  merchantTransactionId")
                    }
                }
            }

            override fun onShippingResult(merchantTransactionId: String, status: Boolean) {
                ILog.e(TAG, "$merchantTransactionId shipping result:$status")
            }

            override fun onStoreInitialized(initState: Boolean) {

            }
        }).setCustomerListener(object : IHelperCallback {
            override fun onUnreadHelperMessageCount(count: Int) {

            }
        }).setAuthListener(object : IAuthResult {
            override fun onLogout(platform: String) {
                IToast.toast("sign out:$platform")
            }

            override fun onLoginResult(platform: String, status: Boolean, channel: String?, reason: String?) {
                IToast.toast("sign result=$status; platform=$platform; reason=$reason")
            }
        })
            .build()
        AndroidSdk.onCreate(this, builder)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        AndroidSdk.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        AndroidSdk.onPause()
    }

    fun onClick(view: View) {
        val id = view.id
        when (id) {
            R.id.log_event -> {
                IvySdk.Instance.logEvent("ttest", EventType.EVENT_TYPE_COMMON, EventSrc.EVENT_SRC_CLIENT)
             // GridManager.Instance.test()

            }
            R.id.has_banner -> {
                val status = IvySdk.Instance.hasBannerAd()
                IToast.toast("has banner:$status")
            }

            R.id.show_banner -> {
                val pos = abs(Random.nextInt()) % 8 + 1
                val json = JSONObject()
                json.put("test", "test")
                json.put("test1", false)
                IvySdk.Instance.showBannerAd(pos, "default", 1, json.toString())
            }
            R.id.close_banner -> IvySdk.Instance.closeBannerAd(1)
            R.id.has_full -> {
                val status = IvySdk.Instance.hasInterstitialAd()
                IToast.toast("has full:$status")
            }

            R.id.show_full -> IvySdk.Instance.showInterstitialAd("")
            R.id.has_video -> {
                val status = IvySdk.Instance.hasRewardedAd()
                IToast.toast("has reward:$status")
            }

            R.id.show_video -> {
                val json = JSONObject()
                json.put("test", "test")
                json.put("test1", false)
                IvySdk.Instance.showRewardedAd(tag = "testTag", placement = 1, clientInfo = json.toString())
            }
            R.id.sign_play_games -> IvySdk.Instance.loginPlayGames()
            R.id.play_games_sign_status -> {
                val status = IvySdk.Instance.hasPlayGamesSigned()
                IToast.toast("play-games signed: $status")
            }

            R.id.play_games_user -> {
                val status = IvySdk.Instance.getPlayGamesUserInfo()
                IToast.toast("play-games user: $status")
            }

            R.id.sign_facebook -> IvySdk.Instance.loginFacebook()
            R.id.facebook_user -> {
                val status = IvySdk.Instance.getFacebookUserInfo()
                IToast.toast("facebook user: $status")
            }

            R.id.facebook_sign_status -> {
                val status = IvySdk.Instance.isFacebookLogged()
                IToast.toast("facebok signed: $status")
            }

            R.id.sign_out_facebook -> IvySdk.Instance.logoutFacebook()
            R.id.pay -> IvySdk.Instance.pay(4, null, null)
            R.id.query_purchases -> IvySdk.Instance.queryPurchase(-1)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

}
