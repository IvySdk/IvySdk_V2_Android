package com.ivy.sdk.base.notification

interface INotificationEvent {

    fun onReceivedNotificationAction(action: String)

}