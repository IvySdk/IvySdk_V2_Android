package com.ivy.sdk.base.track

object IKeys {
        const val KEY_TOTAL_AD_REVENUE = "mkv_total_ad_revenue"
        const val KEY_AD_ROAS = "ad_roas"
        const val KEY_KWAI_TOTAL_AD_REVENUE = "mkv_kivi_total_ad_revenue"
        const val KEY_KWAI_AD_SHOW_TIMES = "kwai_total_ad_show_times"
        const val KEY_FIRST_OPEN_TIME = "_first_start_timestamp"
        const val KEY_APP_START_TIMES = "_app_start_times"
        const val KEY_AF_MEDIA_SOURCE = "_af_media_source_"
        const val KEY_AF_CAMPAIGN_ID = "_af_campaign_id_"
        const val KEY_GOOGLE_ADVERTISING_ID = "google_play_advertising_id"
        const val KEY_PAYING_USER = "_paying_user_"
}

object EventSrc {
        const val EVENT_SRC_COMBINATION = "combination"
        const val EVENT_SRC_SDK = "sdk"
        const val EVENT_SRC_CLIENT = "app"
}

object EventType {
        const val EVENT_TYPE_COMMON = "common"
        const val EVENT_TYPE_AD_REVENUE = "ad_revenue"
        const val EVENT_TYPE_PURCHASE = "purchase"
}

object EventParams {

        const val EVENT_PARAM_EVENT_SRC = "event_src"
        const val EVENT_PARAM_ROLE_ID = "roleId"

        //计费

        const val EVENT_PARAM_PAY_STATE = "pay_state"
        const val EVENT_PARAM_PAY_CHANNEL = "pay_channel"
        const val EVENT_PARAM_ORDER_ID = "order_id"
        const val EVENT_PARAM_PRODUCT_SKU = "product_sku"
        const val EVENT_PARAM_PRODUCT_TYPE = "product_type"
        const val EVENT_PARAM_PRODUCT_COUNT = "product_count"
        const val EVENT_PARAM_REVENUE = "revenue"
        const val EVENT_PARAM_VALUE = "value"
        const val EVENT_PARAM_CURRENCY = "currency"
        const val EVENT_PARAM_FAIL_REASON = "fail_reason"

        //ad
        const val EVENT_PARAM_COUNTRY = "country"
        const val EVENT_PARAM_FLOW_SEQ = "flow_seq"
        const val EVENT_PARAM_AD_UNIT = "ad_unit"
     //   const val EVENT_PARAM_AD_TYPE = "ad_type"
        const val EVENT_PARAM_AD_PLACEMENT = "ad_placement"
        const val EVENT_PARAM_AD_RESPONSE_LATENCY = "ad_response_latency"
        const val EVENT_PARAM_AD_FORMAT = "ad_format"
        const val EVENT_PARAM_AD_MEDIATION = "mediation"
        const val EVENT_PARAM_AD_NETWORK = "ad_network"
        const val EVENT_PARAM_AD_SHOW_DURATION = "ad_duration"
        const val EVENT_PARAM_AD_SOURCE_INSTANCE = "ad_source_instance"
        const val EVENT_PARAM_AD_MEDIATION_GROUP = "mediation_group"
        const val EVENT_PARAM_AD_MEDIATION_AB_TEST = "mediation_ab_test"

}


object EventIDs {

        const val EVENT_APP_OPEN = "sdk_app_open"
        const val SDK_FIRST_APP_OPEN = "sdk_first_open"
        const val SDK_FIRST_AF_APP_OPEN_ATTRIBUTION = "sdk_af_app_open_attribution"
        const val SDK_FIRST_AF_CONVERSION = "sdk_af_conversion"
        const val GMS_AD_PAID = "gms_ad_paid_event"

        const val PLATFORM_INIT_START = "sdk_init_start"
        const val PLATFORM_INIT_COMPLETED = "sdk_init_complete"
        const val AD_REQUEST = "sdk_ad_request"
        const val AD_LOAD_SUCCESS = "sdk_ad_loaded"
        const val AD_LOAD_FAILED = "sdk_ad_load_failed"
        const val AD_SHOW_SUCCESS = "sdk_ad_shown"
        const val AD_SHOW_FAILED = "sdk_ad_show_failed"
        const val AD_CLICKED = "sdk_ad_clicked"
        const val AD_CLOSED = "sdk_ad_closed"
        const val AD_REWARD_USER = "sdk_ad_rewarded"
        const val AD_IMPRESSION_REVENUE = "sdk_ad_revenue"
        const val AD_IMPRESSION_REVENUE_FIREBASE = "ad_impression_revenue"
        const val AD_REACH_PAGE = "sdk_ad_fired"

        const val IAP_PRE_ORDER = "sdk_iap_pre_order"
        const val IAP_VERIFICATION = "sdk_iap_verification"
        const val IAP_PURCHASED = "sdk_iap_purchased"
        const val IAP_USER_CANCEL = "sdk_iap_canceled"

        const val DEVICE_INFO = "sdk_device_info"


}