package com.ivy.sdk.core

import com.ivy.sdk.base.ads.IAdListener
import com.ivy.sdk.base.billing.IPurchaseResult
import com.ivy.sdk.base.game.auth.IAuthResult
import com.ivy.sdk.base.helper.IHelperCallback
import com.ivy.sdk.base.notification.INotificationEvent

class Builder private constructor() {

    var adListener: IAdListener? = null
    var purchaseResult: IPurchaseResult? = null
    var helperListener: IHelperCallback? = null
    var authResult: IAuthResult? = null
    var notificationEvent: INotificationEvent? = null

    class Build {
        private var builder: Builder = Builder()

        fun setAdListener(adListener: IAdListener): Build {
            builder.adListener = adListener
            return this
        }

        fun setPurchaseListener(purchaseResult: IPurchaseResult): Build {
            builder.purchaseResult = purchaseResult
            return this
        }

        fun setCustomerListener(callback: IHelperCallback): Build {
            builder.helperListener = callback
            return this
        }

        fun setAuthListener(callback: IAuthResult): Build {
            builder.authResult = callback
            return this
        }

        fun setNotificationEventListener(callback: INotificationEvent): Build {
            builder.notificationEvent = callback
            return this
        }


        fun build(): Builder = builder
    }

}