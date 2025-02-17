package com.ivy.sdk.google.pay

import com.android.billingclient.api.ProductDetails
import com.ivy.sdk.base.billing.AbsGoods
import org.json.JSONException
import org.json.JSONObject

class Goods : AbsGoods<ProductDetails>() {

    var offerId: String? = null //google billing中订阅型消费品校验id
    var offerToken: String? = null //google billing中订阅型消费品校验key

    var productDetails: ProductDetails? = null

    companion object {
        fun decode(id: Int, data: JSONObject?): Goods? = Goods().decodeConfig(id, data) as? Goods
    }

    override fun update(t: ProductDetails, callback: (t: ProductDetails) -> Unit) {
        this.productDetails = t
        super.update(t, callback)
    }

    override fun toJson(): String = JSONObject().apply {
        try {
            put("id", sku)
            put("type", type)
            put("price", price)
            put("price_amount", priceAmountMicros / 1000000.0f)
            put("original_price", originalPrice)
            put("original_price_amount", originalPriceAmountMicros / 1000000.0f)
            put("currency", currency)
            put("title", title)
            put("desc", desc)
            put("usd", usd)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }.toString()

    override fun getPriceAmount(): Double {
        if (priceAmountMicros == 0L) return usd
        return priceAmountMicros / 1000000.0
    }

    override fun getOriginalPriceAmount(): Double {
        if (originalPriceAmountMicros == 0L) return usd
        return originalPriceAmountMicros / 1000000.0
    }


}