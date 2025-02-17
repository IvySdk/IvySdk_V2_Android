package com.ivy.sdk.core.payment

import android.app.Dialog
import com.ivy.sdk.base.billing.IIPurchase
import com.ivy.sdk.base.billing.IIPurchaseResult
import com.ivy.sdk.base.billing.IPurchaseResult
import com.ivy.sdk.base.grid.GridManager
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.core.utils.LoadingDialog

class IvyPay  : IIPurchase {

    companion object {
        const val TAG = "pay"
      //  val Instance by lazy(LazyThreadSafetyMode.NONE) { IvyPay() }
    }

    private var purchaseImpl: IIPurchase? = null
    private var callback: IPurchaseResult? = null

    private var isPurchasing: Boolean = false
    private var loadingDialog: Dialog? = null

    override fun setup(appId: String, data: String, debug: Boolean, callback: IIPurchaseResult) {
        val platform = GridManager.Instance.getGridConfig("payment_platform", "google_pay")
        when (platform) {
            "google_pay" -> {
                try {
                    purchaseImpl = Class.forName("com.ivy.sdk.google.pay.GoogleBillingPurchase")
                        .getDeclaredConstructor()
                        .newInstance() as? IIPurchase
                    purchaseImpl?.setup(appId, data, debug, callback) ?: ILog.w(TAG, "google billing setup failed")
                } catch (e: Exception) {
                    ILog.e(TAG, "instantiate purchase impl failed:${e.message}")
                }
            }

            else -> ILog.w(TAG, "no payment platform configured!!!")
        }
    }

    override fun shippingGoods(merchantTransactionId: String) {
        purchaseImpl?.shippingGoods(merchantTransactionId)
    }

    override fun isPaymentInitialized(): Boolean = purchaseImpl?.isPaymentInitialized() ?: false

    override fun pay(id: Int, payload: String?, clientInfo: String?) {
        if (isPurchasing) {
            return
        }
        isPurchasing = true
        loadingDialog = LoadingDialog(ActivityUtil.Instance.activity!!)
        loadingDialog!!.show()
        purchaseImpl?.pay(id, payload, clientInfo) ?: callback?.payResult(id, IPurchaseResult.PAY_FAILED, payload, null)
    }

    fun endPay() {
        isPurchasing = false
        try {
            loadingDialog?.dismiss()
            loadingDialog = null
        } catch (_: Exception) {
        }
    }

    override fun getGoodsInfo(id: Int): String = purchaseImpl?.getGoodsInfo(id) ?: run { if (id == -1) "[]" else "{}" }

    override fun queryPurchase(id: Int) = purchaseImpl?.queryPurchase(id) ?: run { }

}