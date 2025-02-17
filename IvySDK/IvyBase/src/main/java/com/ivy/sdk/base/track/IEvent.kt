package com.ivy.sdk.base.track

interface IEvent {

    fun logEvent(
        eventName: String,
        eventType: String,
        eventSrc: String,
        params: MutableMap<String, Any>? = null,
        platforms: List<TrackPlatform>? = null
    )
}

//interface IEvent2 {
//
//    fun logAdRevenue(
//        adNetwork: String,
//        mediation: String,
//        adUnit: String,
//        adType: String,
//        adFormat: String,
//        placement: String,
//        revenue: Double,
//        currency: String,
//        params: Map<String, Any>? = null
//    )
//
//}
//
//interface IEvent3  {
//
//    fun logPurchase(
//        state: PurchaseState,
//        payChannel: String,
//        orderId: String,
//        productSku: String,
//        productType: String,
//        productCount: Int,
//        currency: String?,
//        price: Double,
//        failReason: String?,
//        params: Map<String, Any>? = null
//    )
//
//}
//
//interface IEventCombine : IEvent, IEvent2, IEvent3 {
//
//
//}