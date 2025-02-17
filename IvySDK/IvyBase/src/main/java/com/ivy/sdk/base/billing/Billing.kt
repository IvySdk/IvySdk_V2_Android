package com.ivy.sdk.base.billing

import com.ivy.sdk.base.track.IEvent
import org.json.JSONObject

/**
 * 支付状态
 */
object PurchaseState {
    const val PRE_ORDER: Int = 1
    const val VERIFICATION: Int = 4
    const val PAY_RESULT: Int = 3
}


object PurchaseError {
    const val ERROR_SIGNED_FAILED = "signed_failed"
    const val ERROR_ORDER_FORMAT = "error_order_format"
    const val ERROR_INVALID_PRODUCT = "invalid_product"
    const val ERROR_PRE_ORDER_FAILED = "pre_order_failed"
    const val ERROR_PRE_ORDER_ID = "pre_order_id_invalid"
    const val ERROR_PULL_UP_SHOP = "pull_up_shop_failed"
    const val ERROR_PURCHASE_FAILED_ = "purchase_failed_"
    const val ERROR_KEY_VERIFY_FAILED = "key_verify_failed"
    const val ERROR_VERIFY_FAILED = "verify_failed"
    const val ERROR_CONSUME_FAILED_ = "consume_failed"
    const val ERROR_UNKNOWN = "unknown"


}

object PurchaseResult {

}

///**
// *  通过checkout内 force-check 字段配置
// */
//interface Verify {
//    companion object {
//        const val NONE_VERIFY = 0
//
//        //将订单信息回传客户端，由客户端校验
//        const val CLIENT_VERIFY = 2
//
//        //是否必须校验
//        const val MUST_VERIFY = 1
//
//
//        //订单校验状态码
//        const val ERROR_ORDER_FORMAT: Int = 6
//
//        const val ERROR_SIGNED_FAILED: Int = 1
//
//
//        const val ERROR_RESPONSE_NOT_SUCCESS: Int = 2
//
//        const val ERROR_RESPONSE_EMPTY: Int = 3
//
//        const val ERROR_RESPONSE_WRONG_STATUS: Int = 4
//
//        const val ERROR_VERIFY_SERVER_HTTP_ERROR: Int = 5
//
//        const val SOFT_PURCHASE_ERROR: Int = 10
//
//        const val ERROR_INVALID_PREORDER_ID: Int = 11
//
//
//        fun isClientCheck(code: Int): Boolean = code == CLIENT_VERIFY
//
//        fun isMustCheck(code: Int): Boolean = code == MUST_VERIFY
//
//    }
//}

/**
 * 由商品信息内 repeat字段决定
 */
interface GoodsType {
    companion object {
        // 一次性物品
        const val INAPP = "inapp"

        //订阅
        const val SUBS = "subs"

        //永久商品

    }
}

/**
 * 支付结果
 */
interface IPurchaseResult {
    companion object {
        const val UNSUPPORTED = 0
        const val PAY_SUCCEED = 1
        const val PAY_FAILED = 2
    }

    fun payResult(payId: Int, status: Int, payload: String?, merchantTransactionId: String?)

    fun onShippingResult(merchantTransactionId: String, status: Boolean)

    fun onStoreInitialized(initState: Boolean)
}

interface IIPurchaseResult : IPurchaseResult, IEvent {

}

/**
 * 商品信息
 */
abstract class AbsGoods<T> {
    var id: Int = -1 // pay id
    var title: String = "" //标题
    var desc: String = "" //商品描述
    var sku: String = "" //商品在计费平台唯一id
    var type: String = GoodsType.INAPP // 商品计费类型，参考  GoodType
    var usd: Double = 0.0 //由运营设置的商品价格，默认美元价格，没用货币单位
    var currency: String = "USD" // 默认货币单位
    var price: String = "" // 商品价格，包含货币单位
    var priceAmountMicros: Long = 0L//商品价格，无货币单位，单位：微米， 实际使用时需要除以 1000000.0

    //商品原价，针对打折或试用商品
    var originalPrice: String = ""
    var originalPriceAmountMicros: Long = 0L

    //是否已同步线上计费点信息
    var hasSync: Boolean = false


    fun decodeConfig(id: Int, data: JSONObject?): AbsGoods<T>? = data?.let { json ->
        apply {
            this.id = id
            sku = json.optString("feename")
            if (sku.isEmpty()) return@let null
            title = json.optString("title")
            desc = json.optString("desc")
            type = if (json.optInt("repeat", 1) == 1) GoodsType.INAPP else GoodsType.SUBS
            usd = json.optDouble("usd")
            price = "US$${usd}"
        }
    }

    open fun update(t: T, callback: (t: T) -> Unit) {
        callback.invoke(t)
        hasSync = true
    }

//    abstract fun <T, K> update(t: T, callback: (k: K, t: T) -> Unit)

    abstract fun toJson(): String

    override fun toString(): String = toJson()

    abstract fun getPriceAmount(): Double
    abstract fun getOriginalPriceAmount(): Double

}