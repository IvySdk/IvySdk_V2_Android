package com.ivy.sdk.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseCloudMessageService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        //do nothing...
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // need send to server?
    }

}