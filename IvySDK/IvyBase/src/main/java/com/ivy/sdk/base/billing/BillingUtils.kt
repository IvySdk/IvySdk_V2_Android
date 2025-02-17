package com.ivy.sdk.base.billing

import com.ivy.sdk.base.net.HttpUtil
import com.ivy.sdk.base.storage.LocalStorage
import com.ivy.sdk.base.utils.ILog
import com.ivy.sdk.base.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.InvalidObjectException
import java.util.Locale


data class MissedOrder(
    val appId: String,
    val uuid: String,
    val rpDate: String,
    val country: String,
    val merchantTransactionId: String,
    val orderId: String,
    val environment: String,
    val orderStatus: Int,
    val productId: String,
    val name: String,
    val usd: Double
)

class BillingUtils<T> constructor(
    private val appId: String,
    private val baseUrl: String? = null,
    private val debug: Boolean = false
) {

    companion object {
        const val TAG = "billing"
    }

    private fun formatUrl(action: String) = baseUrl?.let {
        return@let if (it.endsWith("/")) {
            "${it}${action}"
        } else {
            "$it/${action}"
        }
    } ?: ""

    fun preOrder(good: AbsGoods<T>, callback: (status: Boolean, goods: AbsGoods<T>, merchantTransactionId: String?) -> Unit) {
        try {
            val param = JSONObject()
            param.put("is_encrypt", false)
            val body = JSONObject()
            body.put("app_id", appId)
            body.put("uuid", Util.roleId())
            body.put("is_sandbox", debug)
            body.put("country", Locale.getDefault().country)
            val skuJson = JSONObject()
            skuJson.put("id", good.sku)
            skuJson.put("price", good.price)
            skuJson.put("price_amount", good.getPriceAmount())
            skuJson.put("currency", good.currency)
            skuJson.put("name", good.title)
            skuJson.put("desc", good.desc)
            skuJson.put("usd", good.usd)
            body.put("sku_json", skuJson)
            param.put("data", body)
            val url = formatUrl("pre_google")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = postRequest(url, param.toString())
                    launch(Dispatchers.Main) {
                        try {
                        val merchantTransactionId = data?.let { JSONObject(it).getJSONObject("data").getString("merchant_transaction_id") }
                        if (merchantTransactionId.isNullOrEmpty()) {
                            callback.invoke(false, good, null)
                        } else {
                            encodeMerchantTransactionId(good.sku, merchantTransactionId)
                            callback.invoke(true, good, merchantTransactionId)
                        }
                        } catch (_: Exception) {
                            callback.invoke(false, good, null)
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        ILog.e(TAG, "pre order err for:${good.id}; err:${e.message}")
                        callback.invoke(false, good, null)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "pre order err for:${good.id}; err:${e.message}")
            callback.invoke(false, good, null)
        }
    }

    fun purchasing() {

    }

    @Throws(Exception::class)
    fun verifyOrder(merchantTransactionId: String, signature: String, purchaseData: String, callback: (state: Boolean) -> Unit) {
        try {
            val param = JSONObject()
            param.put("is_encrypt", false)
            val body = JSONObject()
            body.put("app_id", appId)
            body.put("uuid", Util.roleId())
            body.put("inapp_data_signature", signature)
            body.put("merchant_transaction_id", merchantTransactionId)
            body.put("inapp_purchase_data", JSONObject(purchaseData))
            param.put("data", body)
            val url = formatUrl("verify_google")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = postRequest(url, param.toString())
                    val status = data?.let { JSONObject(it).getBoolean("data") } ?: false
                    launch(Dispatchers.Main) {
                        callback.invoke(status)
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        ILog.e(TAG, "verify order failed:${e.message}")
                        callback.invoke(false)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "verify order failed:${e.message}")
            callback.invoke(false)
        }
    }

    @Throws(Exception::class)
    fun receiptGoods(merchantTransactionId: String, callback: (state: Boolean) -> Unit) {
        try {
            val param = JSONObject()
            param.put("is_encrypt", false)
            val body = JSONObject()
            body.put("app_id", appId)
            body.put("uuid", Util.roleId())
            body.put("merchant_transaction_id", merchantTransactionId)
            param.put("data", body)
            val url = formatUrl("consume_google")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = postRequest(url, param.toString())
                    val status = data?.let { JSONObject(it).getBoolean("data") } ?: false
                    launch(Dispatchers.Main) {
                        callback.invoke(status)
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        ILog.e(TAG, "receipt goods failed:${e.message}")
                        callback.invoke(false)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "receipt goods failed:${e.message}")
            callback.invoke(false)
        }
    }

    fun unShippedGoodsOrders(callback: (data: List<MissedOrder>?) -> Unit) {
        try {
            val url = formatUrl("unconsume_google")
            val uuid = Util.roleId()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = getRequest("$url?app_id=$appId&uuid=$uuid")
                    val datas = data?.let { JSONObject(it).getJSONArray("data") }
                    if (datas != null) {
                        val missedOrders = mutableListOf<MissedOrder>()
                        val count = datas.length()
                        for (index in 0 until count) {
                            val json = datas.getJSONObject(index)
                            try {
                                val itemAppId = json.getString("app_id")
                                if (!itemAppId.equals(appId)) {
                                    continue
                                }
                                val itemUUID = json.getString("uuid")
                                if (!itemUUID.equals(uuid)) {
                                    continue
                                }
                                val rpDate = json.getString("rp_date")
                                val country = json.getString("country")
                                val merchantTransactionId = json.getString("merchant_transaction_id")
                                if (merchantTransactionId.isNullOrEmpty()) {
                                    continue
                                }
                                val orderId = json.getString("order_id")
                                if (orderId.isNullOrEmpty()) {
                                    continue
                                }
                                val environment = json.getString("environment")
                                val orderStatus = json.getInt("order_status")
                                if (orderStatus != 3) {
                                    continue
                                }
                                val productId = json.getString("product_id")
                                if (productId.isNullOrEmpty()) {
                                    continue
                                }
                                val name = json.getString("name")
                                val usd = json.getDouble("usd")
                                val itemMissedOrder =
                                    MissedOrder(
                                        itemAppId, itemUUID, rpDate, country, merchantTransactionId, orderId,
                                        environment, orderStatus, productId, name, usd
                                    )
                                missedOrders.add(itemMissedOrder)
                            } catch (e: Exception) {
                                ILog.e(TAG, "check missed order err:${e.message}")
                            }
                        }
                        launch(Dispatchers.Main) {
                            callback.invoke(missedOrders)
                        }
                    } else {
                        throw InvalidObjectException("response data invalid")
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        ILog.e(TAG, "check unreceived orders failed:${e.message}")
                        callback.invoke(null)
                    }
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "check unreceived orders failed:${e.message}")
            callback.invoke(null)
        }
    }

    private fun encodeMerchantTransactionId(sku: String, merchantTransactionId: String) {
        LocalStorage.Instance.encodeString("merchant_transaction_$sku", merchantTransactionId)
    }

    fun decodeMerchantTransactionId(sku: String): String? = LocalStorage.Instance.decodeString("merchant_transaction_$sku")

    private fun getRequest(url: String): String? {
        val okHttpClient = HttpUtil.Instance.okHttpClient
        val request = Request.Builder().url(url).get().build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return response.body?.string()
        }
        return null
    }

    private fun postRequest(url: String, param: String): String? {
        val okHttpClient = HttpUtil.Instance.okHttpClient
        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        val postBody = param.toRequestBody(mediaType)//FormBody.Builder().add("data", param).build()
        val request = Request.Builder().url(url).post(postBody).build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            return response.body?.string()
        }

        return null
    }

}