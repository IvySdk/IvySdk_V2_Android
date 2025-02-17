package com.ivy.sdk.base.billing

interface IPurchase {
    fun isPaymentInitialized(): Boolean

    fun pay(id: Int, payload: String?, clientInfo: String?)

    fun shippingGoods(merchantTransactionId: String)

    fun getGoodsInfo(id: Int = -1): String

    fun queryPurchase(id: Int = -1)
}

interface IIPurchase : IPurchase {
    fun setup(appId: String, data: String, debug: Boolean, callback: IIPurchaseResult)

}

abstract class AbsPurchase<T> : IIPurchase {

    open lateinit var TAG: String

    protected val goodsData: MutableMap<String, T> = mutableMapOf() //<sku, AbsGood>
    protected val ids: MutableMap<Int, String> = mutableMapOf() // <payId, sku>
    protected var purchaseResultCallback: IIPurchaseResult? = null
    protected var initializeStatus = false
    protected var appId: String = ""

    protected var debug: Boolean = false

    //========== 支付结果校验 =========
//    protected var verifyType: Int = Verify.NONE_VERIFY
    protected var verifyKey: String? = null
    protected var verifyServerUrl: String? = null
    //========== 支付结果校验 =========

    override fun setup(appId: String, data: String, debug: Boolean, callback: IIPurchaseResult) {
        this.appId = appId
        this.debug = debug
        this.purchaseResultCallback = callback
    }

    override fun isPaymentInitialized(): Boolean = initializeStatus


}