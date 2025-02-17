package com.ivy.sdk.google.pay

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.ivy.sdk.base.App
import com.ivy.sdk.base.ads.IAdLoader
import com.ivy.sdk.base.billing.AbsPurchase
import com.ivy.sdk.base.billing.BillingUtils
import com.ivy.sdk.base.billing.GoodsType
import com.ivy.sdk.base.billing.IIPurchaseResult
import com.ivy.sdk.base.billing.IPurchaseResult
import com.ivy.sdk.base.billing.PurchaseError
import com.ivy.sdk.base.billing.PurchaseState
import com.ivy.sdk.base.grid.GridManager
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.track.EventIDs
import com.ivy.sdk.base.track.EventParams
import com.ivy.sdk.base.track.EventSrc
import com.ivy.sdk.base.track.EventType
import com.ivy.sdk.base.utils.ActivityUtil
import com.ivy.sdk.base.utils.ILog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object Result {
    const val SUCCESS: Boolean = true
    const val FAILURE: Boolean = false
}

open class GoogleBillingPurchase : AbsPurchase<Goods>(), PurchasesUpdatedListener {

    override var TAG: String = "GABilling"
    private var currentPayingId: Int = -1
    private var billingUtils: BillingUtils<ProductDetails>? = null
    private var clientInfos: MutableMap<String, Any> = mutableMapOf()

    private var billingClient: BillingClient =
        BillingClient.newBuilder(App.Instance).enablePendingPurchases().setListener(this@GoogleBillingPurchase).build()


    override fun setup(appId: String, data: String, debug: Boolean, callback: IIPurchaseResult) {
        super.setup(appId, data, debug, callback)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val json = JSONObject(data)
//            verifyType = json.optInt("force-check", Verify.NONE_VERIFY)
                //gp后台提供的校验key
                verifyKey = json.optString("key")
                //校验地址
                verifyServerUrl = json.optString("verify-url")
                if (!verifyServerUrl.isNullOrBlank()) {
                    billingUtils = BillingUtils(appId, verifyServerUrl, debug)
                }
                json.optJSONObject("checkout")?.let { checkout ->
                    checkout.keys().forEach { key ->
                        try {
                            val payId = key.toInt()
                            Goods.decode(payId, checkout.optJSONObject(key))?.let { item ->
                                ids[payId] = item.sku
                                goodsData[item.sku] = item
                            } ?: ILog.e(TAG, "invalid sku for $key")
                        } catch (e: NumberFormatException) {
                            ILog.e(TAG, "invalid pay id:${key}")
                        }
                    }
                    launch(Dispatchers.Main) {
                        connectGoogleBilling()
                    }
                } ?: ILog.w(TAG, "no goods config!!!")
            } catch (e: Exception) {
                ILog.w(TAG, "invalid payment config")
            }
        }
    }

    private fun connectGoogleBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                ILog.i(TAG, "billing store disconnected")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                //查询所有商品信息
                ILog.i(TAG, "billing store connect result: ${p0.responseCode}")
                if (p0.responseCode == BillingResponseCode.OK) {
                    queryGoods(null, success = {
                        purchaseResultCallback?.onStoreInitialized(true)
                        //查询掉单商品
                        queryPurchase(-1)
                    }, failure = {
                        //不回传失败状态
//                                purchaseResultCallback?.onStoreInitialized(false)
                        queryPurchase(-1)
                    })
                }
            }
        })
    }

    /**
     * 查询商品信息
     * @param sku           商品sku，如为null，则查询所有商品信息
     */

    private fun queryGoods(sku: String? = null, success: (() -> Unit)? = null, failure: (() -> Unit)? = null) {
        when (billingClient.connectionState) {
            ConnectionState.CONNECTED -> {
                sku?.let { itemSku ->
                    ILog.i(TAG, "billing client connected; start query goods:${itemSku}")
                    goodsData[itemSku]?.let {
                        val list: List<Product> = listOf(
                            Product.newBuilder()
                                .setProductId(it.sku)
                                .setProductType(if (it.type == GoodsType.INAPP) ProductType.INAPP else ProductType.SUBS)
                                .build()
                        )
                        queryGoodsDetails(list, success, failure)
                    } ?: ILog.w(TAG, "unable to load goods info for $sku; not configured!!")
                } ?: run {
                    ILog.i(TAG, "billing client connected; start query all goods")
                    val list: List<Product> = goodsData.map {
                        Product.newBuilder()
                            .setProductId(it.value.sku)
                            .setProductType(if (it.value.type == GoodsType.INAPP) ProductType.INAPP else ProductType.SUBS)
                            .build()
                    }
                    queryGoodsDetails(list, success, failure)
                }
            }

            ConnectionState.CLOSED, ConnectionState.DISCONNECTED -> {
                ILog.w(TAG, "billing client closed or disconnected! start re-connect")
                connectGoogleBilling()
                failure?.invoke()
            }

            ConnectionState.CONNECTING -> {
                ILog.i(TAG, "billing client connecting; waiting...")
                failure?.invoke()
            }
        }
    }

    private fun queryGoodsDetails(list: List<Product>, success: (() -> Unit)? = null, failure: (() -> Unit)? = null) {
        val queryParams = QueryProductDetailsParams.newBuilder().setProductList(list).build()
        billingClient.queryProductDetailsAsync(queryParams) { billingResult, productDetails ->
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> {
                    when (productDetails.isEmpty()) {
                        true -> {
                            ILog.i(TAG, "empty product details response")
                            failure?.invoke()
                        }

                        false -> {
                            productDetails.forEach { details ->
                                goodsData[details.productId]?.let { good ->
                                    good.update(details) { t ->
                                        good.sku = t.productId
                                        good.title = t.title
                                        good.desc = t.description
                                        when (t.productType) {
                                            ProductType.INAPP -> {
                                                good.type = GoodsType.INAPP
                                                t.oneTimePurchaseOfferDetails?.let {
                                                    good.price = it.formattedPrice
                                                    good.priceAmountMicros = it.priceAmountMicros
                                                    good.currency = it.priceCurrencyCode
                                                }
                                            }

                                            ProductType.SUBS -> {
                                                good.type = GoodsType.SUBS
                                                t.subscriptionOfferDetails?.getOrNull(0)?.let {
                                                    good.offerId = it.offerId
                                                    good.offerToken = it.offerToken
                                                    it.pricingPhases.pricingPhaseList.getOrNull(0)?.let { pricingPhase ->
                                                        good.price = pricingPhase.formattedPrice
                                                        good.currency = pricingPhase.priceCurrencyCode
                                                        good.priceAmountMicros = pricingPhase.priceAmountMicros
                                                        // 查询下个计费周期或试用期结束后价格
                                                        it.pricingPhases.pricingPhaseList.getOrNull(1)?.let { pricing ->
                                                            good.originalPrice = pricing.formattedPrice
                                                            good.originalPriceAmountMicros = pricing.priceAmountMicros
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } ?: ILog.e(TAG, "no good find for ${details.productId}")
                            }
                            ILog.i(TAG, "goods info loaded")
                            success?.invoke()
                        }
                    }
                }

                else -> {
                    ILog.w(TAG, "query product details err:${billingResult.responseCode}")
                    failure?.invoke()
                }
            }
        }
    }

    override fun isPaymentInitialized(): Boolean = billingClient.isReady

    override fun shippingGoods(merchantTransactionId: String) {
        billingUtils?.receiptGoods(merchantTransactionId) { state ->
            ILog.i(TAG, "shipping goods result:$state")
            purchaseResultCallback?.onShippingResult(merchantTransactionId, state)
        }
    }

    override fun pay(id: Int, payload: String?, clientInfo: String?) {
        if (billingClient.connectionState != ConnectionState.CONNECTED) {
            connectGoogleBilling()
            ILog.w(TAG, "billing client is not ready")
            purchaseResultCallback?.payResult(id, IPurchaseResult.PAY_FAILED, payload, null)
            return
        }
        val activity = ActivityUtil.Instance.activity
        if (activity == null) {
            ILog.w(TAG, "invalid activity to pay")
            purchaseResultCallback?.payResult(id, IPurchaseResult.PAY_FAILED, payload, null)
            return
        }
        ids[id]?.let { sku ->
            goodsData[sku]?.let { goods ->
                if (!goods.hasSync) {
                    queryGoods(goods.sku)
                    ILog.w(TAG, "has not sync good message:${id};${sku}")
                    purchaseResultCallback?.payResult(id, IPurchaseResult.PAY_FAILED, payload, null)
                    return
                }
                encodePayload(sku, payload)
                this.setupClientInfo(clientInfo)
                billingUtils?.let { helper ->
                    helper.preOrder(goods, callback = { status, _, merchantTransactionId ->
                        if (status) {
                            if (merchantTransactionId.isNullOrBlank()) {
                                setupPurchaseResult(PurchaseState.PRE_ORDER, Result.FAILURE, goods, null, PurchaseError.ERROR_PRE_ORDER_ID)
                            } else {
                                setupPurchaseResult(
                                    PurchaseState.PRE_ORDER,
                                    Result.SUCCESS,
                                    goods,
                                    null,
                                    null,
                                    mutableMapOf<String, Any>("merchantTransactionId" to merchantTransactionId)
                                )
                                doPay(activity, id, goods, payload, merchantTransactionId)
                            }
                        } else {
                            setupPurchaseResult(PurchaseState.PRE_ORDER, Result.FAILURE, goods, null, PurchaseError.ERROR_PRE_ORDER_FAILED)
                        }
                    })
                } ?: doPay(activity, id, goods, payload, null)
            } ?: {
                ILog.w(TAG, "no goods for pay id:${id} & sku:${sku} setup! check config file")
                setupPurchaseResult(PurchaseState.PRE_ORDER, Result.FAILURE, null, null, PurchaseError.ERROR_INVALID_PRODUCT)
            }
        } ?: {
            ILog.w(TAG, "no pay id:${id} setup")
            setupPurchaseResult(PurchaseState.PRE_ORDER, Result.FAILURE, null, null, PurchaseError.ERROR_INVALID_PRODUCT)
        }
    }

    /**
     * 调用支付
     */
    private fun doPay(activity: Activity, id: Int, goods: Goods, payload: String?, merchantTransactionId: String?) {
        billingUtils?.purchasing()
        currentPayingId = id
        val productDetailsParamBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
        goods.offerToken?.let { productDetailsParamBuilder.setOfferToken(it) }
        goods.productDetails?.let { productDetailsParamBuilder.setProductDetails(it) }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamBuilder.build()))
            .setIsOfferPersonalized(true).build()
        CoroutineScope(Dispatchers.Main).launch {
            val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
            when (billingResult.responseCode) {
                BillingResponseCode.OK -> ILog.i(TAG, "start pay succeed! waiting for check")

                BillingResponseCode.ITEM_ALREADY_OWNED -> ILog.i(TAG, "already owned the good${id}--${goods.sku}! waiting for consume")

                else -> {
                    ILog.i(TAG, "start pay failed:${billingResult.responseCode} for ${id}--${goods.sku}")
                    if (merchantTransactionId.isNullOrEmpty()) {
                        setupPurchaseResult(PurchaseState.PAY_RESULT, Result.FAILURE, goods, null, PurchaseError.ERROR_PULL_UP_SHOP)
                    } else {
                        setupPurchaseResult(
                            PurchaseState.PAY_RESULT,
                            Result.FAILURE,
                            goods,
                            null,
                            PurchaseError.ERROR_PULL_UP_SHOP,
                            mutableMapOf<String, Any>("merchantTransactionId" to merchantTransactionId)
                        )
                    }
                }
            }
        }
    }

    /**
     * 获取商品信息
     */
    override fun getGoodsInfo(id: Int): String {
        try {
            if (id == -1) {
                try {
                    val data = JSONArray()
                    goodsData.forEach { item -> data.put(item.value.toString()) }
                    return data.toString()
                } catch (e: Exception) {
                    ILog.i(TAG, "error when get good info:${e.message}")
                    return "[]"
                }
            }
            return goodsData[ids[id]]?.toString() ?: "{}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "{}"
    }

    /**
     * 查询商品购买状态
     * @param id        商品id， 如为 -1，则查询所有商品
     */
    override fun queryPurchase(id: Int) {
        if (id == -1) {
            queryUnShippedGoods()
            queryUnConsumedPurchases(false)
            queryUnConsumedPurchases(true)
        } else {
            ids[id]?.let { sku ->
                goodsData[sku]?.let { good ->
                    when (good.hasSync) {
                        true -> queryUnConsumedPurchases(good.type != GoodsType.INAPP)
                        false -> {
                            queryGoods(sku, success = {
                                queryUnConsumedPurchases(good.type != GoodsType.INAPP)
                            })
                        }
                    }
                } ?: ILog.i(TAG, "unable to query id! no good found! checkout had it configured")
            } ?: ILog.i(TAG, "unable to query id! no sku found! checkout had it configured")
        }
    }

    /**
     * 查询已购买，但是后台未记录发货的商品
     *
     * 需要客户端及时调用 shippingGoods 接口，否则会存在重复发货的情况
     *
     */
    private fun queryUnShippedGoods() {
        billingUtils?.unShippedGoodsOrders { data ->
            data?.let { list ->
                for (item in list) {
                    val sku = item.productId
                    val id: Int = goodsData[sku]?.id ?: 0
                    if (id == 0) continue
                    val payload = decodePayload(sku)
                    purchaseResultCallback?.payResult(id, IPurchaseResult.PAY_SUCCEED, payload, item.merchantTransactionId)
                }
            }
        }
    }


    /**
     * 记录payload
     */
    private fun encodePayload(sku: String, payload: String?) {
        payload?.let {
            LocalStorage.Instance.encodeString("payload_${sku}", it)
        } ?: LocalStorage.Instance.removeValueForKey("payload_${sku}")
    }

    /**
     * 删除payload
     */
    private fun decodePayload(sku: String): String? =
        LocalStorage.Instance.decodeString("payload_${sku}", null)

    /**
     * 支付结果回调
     */
    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        // 监听付费结果
        ILog.i(TAG, "received purchase result code:${p0.responseCode}; $p0")
        p1?.let { purchases ->
            purchases.forEach { handlePurchase(p0.responseCode, it) }
        } ?: run {
            if (p0.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
                ILog.i(TAG, "check unconsumed goods with purchase response code:${p0.responseCode}")
                queryUnConsumedPurchases(false)
            } else {
                ILog.i(TAG, "default as user canceled purchase response code=${p0.responseCode}")
                setupPurchaseResult(PurchaseState.PAY_RESULT, Result.FAILURE, null, null, "user_cancel")
            }
        }
    }

    /**
     *  解析支付结果
     */
    private fun handlePurchase(responseCode: Int, purchase: Purchase) {
        when (responseCode) {
            BillingResponseCode.OK -> {
                ILog.i(TAG, "purchase success")
                consumeGood(purchase)
            }

            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                ILog.i(TAG, "purchase item already owned")
                consumeGood(purchase)
            }
            else -> {
                val sku = purchase.products[0]
                ILog.i(TAG, "$sku purchase failed;code:${responseCode}")
                val googds = goodsData[sku]
                setupPurchaseResult(
                    PurchaseState.PAY_RESULT,
                    Result.FAILURE,
                    googds,
                    purchase,
                    "${PurchaseError.ERROR_PURCHASE_FAILED_}$responseCode"
                )
            }
        }
    }

    /**
     * 消耗或确认商品
     */
    private fun consumeGood(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            ILog.i(TAG, "good:${purchase.products.getOrNull(0)} consume failed; had not purchased!!!")
            return
        }

        val sku = purchase.products[0]
        val merchantTransactionId = billingUtils?.decodeMerchantTransactionId(sku)
        var params: MutableMap<String, Any>? = null
        if (!merchantTransactionId.isNullOrEmpty()) {
            params = mutableMapOf<String, Any>("merchantTransactionId" to merchantTransactionId)
        }
        val goods = goodsData[sku]
        if (goods == null) {
            setupPurchaseResult(PurchaseState.VERIFICATION, Result.FAILURE, null, purchase, PurchaseError.ERROR_VERIFY_FAILED, params)
            return
        }

        when (goods.type) {
            GoodsType.INAPP -> {
                verifyPurchase(goods, sku, purchase, params, success = {
                    val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    billingClient.consumeAsync(consumeParams) { billingResult, purchaseToken ->
                        if (billingResult.responseCode == BillingResponseCode.OK) {
                            setupPurchaseResult(PurchaseState.PAY_RESULT, Result.SUCCESS, goods, purchase, null, params)
                        } else {
                            //消耗失败,算作失败，待下次查询处理
                            setupPurchaseResult(
                                PurchaseState.PAY_RESULT,
                                Result.FAILURE,
                                goods,
                                purchase,
                                "${PurchaseError.ERROR_CONSUME_FAILED_}${billingResult.responseCode}",
                                params
                            )
                        }
                    }
                }, failure = { code ->
                    ILog.i(TAG, "verify inapp:${sku} failed with server; code=${code}")
                    // 订单验证失败
                    setupPurchaseResult(
                        PurchaseState.PAY_RESULT,
                        Result.FAILURE,
                        goods,
                        purchase,
                        "verify_failed",
                        params
                    )
                })
            }

            else -> {
                when (purchase.isAcknowledged) {
                    true -> {
                        //.. 已使用中的订阅计费点，直接返回成功
                        val payload = decodePayload(sku)
                        purchaseResultCallback?.payResult(goods.id, IPurchaseResult.PAY_SUCCEED, payload, merchantTransactionId)
                    }
                    else -> {
                        verifyPurchase(goods, sku, purchase, params, success = {
                            val acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                                if (billingResult.responseCode == BillingResponseCode.OK) {
                                    setupPurchaseResult(PurchaseState.PAY_RESULT, Result.SUCCESS, goods, purchase, null, params)
                                } else {
                                    //认证失败,算作失败，待下次查询处理
                                    setupPurchaseResult(
                                        PurchaseState.PAY_RESULT,
                                        Result.FAILURE,
                                        goods,
                                        purchase,
                                        "${PurchaseError.ERROR_CONSUME_FAILED_}${billingResult.responseCode}",
                                        params
                                    )
                                }
                            }
                        }, failure = { code ->
                            ILog.i(TAG, "verify subs:${sku} failed with server; code=${code}")
                            //订单验证失败
                            setupPurchaseResult(
                                PurchaseState.PAY_RESULT,
                                Result.FAILURE,
                                goods,
                                purchase,
                                "verify_failed",
                                params
                            )
                        })
                    }
                }
            }
        }
    }


    /**
     * 查询未处理的订单
     */
    private fun queryUnConsumedPurchases(isSubs: Boolean) {
        val queryParams = QueryPurchasesParams.newBuilder().setProductType(if (isSubs) ProductType.INAPP else ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(queryParams) { billingResult, list ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                list.forEach { consumeGood(it) }
            }
        }
    }

    /**
     * 校验订单
     */
    private fun verifyPurchase(
        goods: Goods,
        sku: String,
        purchase: Purchase,
        param: MutableMap<String, Any>?,
        success: (() -> Unit),
        failure: ((code: Int) -> Unit)
    ) {
        val orderId = purchase.orderId
        val orderPrefix = GridManager.Instance.getGridConfig("payment.order.prefix", "")
        if (!orderPrefix.isNullOrEmpty()) {
            if (!orderId.isNullOrEmpty() && !orderId.startsWith(sku) && !orderId.startsWith(orderPrefix)) {
                ILog.e(TAG, "orderId:${orderId} for sku:${sku} not correct")
                failure.invoke(0)
                return
            }
        }
        //    val startTime = System.currentTimeMillis()
        //key校验
        if (!verifyPurchaseWithKey(purchase)) {
            ILog.i(TAG, "key check failed")
            //     val duration = (System.currentTimeMillis() - startTime) / 1000
            setupPurchaseResult(PurchaseState.VERIFICATION, Result.FAILURE, goods, purchase, PurchaseError.ERROR_KEY_VERIFY_FAILED, param)
            failure.invoke(0)
            return
        }
        billingUtils?.let { helper ->
            //server 校验
            val merchantTransactionId = helper.decodeMerchantTransactionId(sku)
            if (merchantTransactionId.isNullOrBlank()) {
                /**
                 *  获取预下单id失败, 此时可能：
                 *  1. 此前的预下单数据丢失
                 *  2. 从无校验切换到在线校验时存在未消耗商品
                 *
                 *  此时 默认为校验已完成，调用消耗API
                 */
                success.invoke()
//                setupPurchaseResult(PurchaseState.VERIFICATION, Result.FAILURE, goods, purchase, PurchaseError.ERROR_PRE_ORDER_ID, param)
//                failure.invoke(0)
            } else {
                helper.verifyOrder(merchantTransactionId, purchase.signature, purchase.originalJson) { result ->
                    if (result) {
                        setupPurchaseResult(PurchaseState.VERIFICATION, Result.SUCCESS, goods, purchase, null, param)
                        success.invoke()
                    } else {
                        // 无论任何原因导致的校验失败，统一默认为支付失败
                        setupPurchaseResult(PurchaseState.VERIFICATION, Result.FAILURE, goods, purchase, PurchaseError.ERROR_UNKNOWN, param)
                        failure.invoke(0)
                    }
                }
            }
        } ?: run {
            //不需要校验
            success.invoke()
        }
    }

    /**
     * 记录支付结果事件
     */
    private fun setupPurchaseResult(
        purchaseState: Int,
        result: Boolean,
        goods: Goods?,
        purchase: Purchase?,
        failReason: String?,
        params: Map<String, Any>? = null
    ) {
        val merchantTransactionId = params?.get("merchantTransactionId") as? String
        when (purchaseState) {
            PurchaseState.PRE_ORDER -> {
                val data: MutableMap<String, Any> = mutableMapOf()
                data["state"] = if (result) 1 else 0
                failReason?.let { data["reason"] = it }
                params?.let { data.putAll(it) }
                if (goods != null) {
                    data["id"] = goods.id
                    data["sku"] = goods.sku
                    data["type"] = goods.type
                    goods.productDetails?.let { detail ->
                        data["title"] = detail.title
                    }
                    data["price"] = goods.price
                    data["currency"] = goods.currency
                    data["price_amount"] = goods.getPriceAmount()
                }
                merchantTransactionId?.let { data["merchantTransactionId"] = it }
                data.putAll(clientInfos)
                data[EventParams.EVENT_PARAM_PAY_STATE] = PurchaseState.PRE_ORDER
                purchaseResultCallback?.logEvent(EventIDs.IAP_PRE_ORDER, EventType.EVENT_TYPE_PURCHASE, EventSrc.EVENT_SRC_SDK, data)
                if (!result && goods != null) {
                    val payload = decodePayload(goods.sku)
                    purchaseResultCallback?.payResult(goods.id, IPurchaseResult.PAY_FAILED, payload, merchantTransactionId)
                }
            }

            PurchaseState.VERIFICATION -> {
                val data: MutableMap<String, Any> = mutableMapOf()
                data["state"] = if (result) 1 else 0
                failReason?.let { data["reason"] = it }
                params?.let { data.putAll(it) }
                goods?.let { g ->
                    data["id"] = g.id
                    data["sku"] = g.sku
                    data["price"] = g.price
                    data["currency"] = goods.currency
                    data["price_amount"] = goods.getPriceAmount()
                }
                var sku: String? = purchase?.products?.get(0)
                val id: Int = goods?.id ?: 0
                if (sku.isNullOrEmpty()) {
                    sku = goods?.sku
                }
                purchase?.let { p ->
                    data["sku"] = p.products[0]
                    data["purchase_state"] = p.purchaseState
                    data["purchase_token"] = p.purchaseToken
                    data["purchase_time"] = p.purchaseTime
                    p.orderId?.let { data["order_id"] = it }
                    data["is_acknowledged"] = if (p.isAcknowledged) 1 else 0
                    data["is_auto_renewing"] = if (p.isAutoRenewing) 1 else 0
                    data["package_name"] = p.packageName
                    data["signature"] = p.signature
                    data["original_json"] = p.originalJson

                }
                merchantTransactionId?.let { data["merchantTransactionId"] = it }
                data.putAll(clientInfos)
                data[EventParams.EVENT_PARAM_PAY_STATE] = PurchaseState.VERIFICATION
                purchaseResultCallback?.logEvent(EventIDs.IAP_VERIFICATION, EventType.EVENT_TYPE_PURCHASE, EventSrc.EVENT_SRC_SDK, data)
                if (!result && sku != null) {
                    val payload = decodePayload(sku)
                    purchaseResultCallback?.payResult(id, IPurchaseResult.PAY_FAILED, payload, merchantTransactionId)
                }
            }

            PurchaseState.PAY_RESULT -> {
                var sku: String? = purchase?.products?.get(0)
                val id: Int = goods?.id ?: 0
                if (sku.isNullOrEmpty()) {
                    sku = goods?.sku
                }
                val payload = if (sku.isNullOrEmpty()) null else decodePayload(sku)
                purchaseResultCallback?.payResult(id, if (result) IPurchaseResult.PAY_SUCCEED else IPurchaseResult.PAY_FAILED, payload, merchantTransactionId)

                if (result) {
                    val orderId = purchase?.orderId
                    if (orderId.isNullOrEmpty()) {
                        return
                    }
                    if (LocalStorage.Instance.containsKey("${orderId}_logged")) {
                        return
                    }
                    LocalStorage.Instance.encodeString("${orderId}_logged", orderId)

                    goods?.let { g ->
                        val total_orders = LocalStorage.Instance.decodeInt("total_orders", 0) + 1
                        val total_revenue = LocalStorage.Instance.decodeDouble("total_revenue", 0.0) + g.getPriceAmount()
                        LocalStorage.Instance.encodeInt("total_orders", total_orders)
                        LocalStorage.Instance.encodeDouble("total_revenue", total_revenue)
                    }

                }

                val data: MutableMap<String, Any> = mutableMapOf()
                data["state"] = if (result) 1 else 0
                failReason?.let { data["reason"] = it }
                params?.let { data.putAll(it) }
                goods?.let { g ->
                    data["id"] = g.id
                    data["sku"] = g.sku
                    data["price"] = g.price
                    data["currency"] = goods.currency
                    data["price_amount"] = goods.getPriceAmount()
                    data["type"] = goods.type
                    data["title"] = goods.title
                    data["price"] = goods.price
                }

                purchase?.let { p ->
                    data["sku"] = p.products[0]
                    data["purchase_state"] = p.purchaseState
                    data["purchase_token"] = p.purchaseToken
                    data["purchase_time"] = p.purchaseTime
                    p.orderId?.let { data["order_id"] = it }
                    data["is_acknowledged"] = p.isAcknowledged
                    data["is_auto_renewing"] = p.isAutoRenewing
                    data["package_name"] = p.packageName
                    data["signature"] = p.signature
                    data["original_json"] = p.originalJson
                }
                merchantTransactionId?.let { data["merchantTransactionId"] = it }
                data.putAll(clientInfos)
                data[EventParams.EVENT_PARAM_PAY_CHANNEL] = "google_pay"
                failReason?.let { data["reason"] = it }
                data[EventParams.EVENT_PARAM_PAY_STATE] = PurchaseState.PAY_RESULT
                purchaseResultCallback?.logEvent(EventIDs.IAP_PURCHASED, EventType.EVENT_TYPE_PURCHASE, EventSrc.EVENT_SRC_SDK, data)
            }
        }
    }

    /**
     *  通过key校验订单
     */
    private fun verifyPurchaseWithKey(purchase: Purchase): Boolean {
        if (verifyKey.isNullOrEmpty()) {
            ILog.w(TAG, "IAP public key is not configured, will NOT verify the purchase")
            return true
        }
        val signature = purchase.signature
        val purchaseData = purchase.originalJson
        val verified: Boolean = Security.verifyPurchase(verifyKey, purchaseData, signature)
        if (!verified) {
            ILog.e(TAG, "purchase key verify failed")
            ILog.i(TAG, "OrderID: ${purchase.orderId}")
            ILog.i(TAG, "Signature: ${purchase.signature}")
            ILog.i(TAG, "PurchaseData: ${purchase.originalJson}")
        } else {
            ILog.i(TAG, "Purchase key Verified success")
        }
        return verified
    }

    private fun setupClientInfo(clientInfo: String?) {
        if (!clientInfo.isNullOrEmpty()) {
            try {
                val json = JSONObject(clientInfo)
                for (key in json.keys()) {
                    val value = json.get(key)
                    if (value is Boolean) {
                        clientInfos[key] = if (value) 1 else 0
                    } else {
                        clientInfos[key] = value
                    }
                }
            } catch (e: Exception) {
                ILog.e(IAdLoader.TAG, "format client info failed:${e.message}")
            }
        } else {
            clientInfos.clear()
        }
    }


}