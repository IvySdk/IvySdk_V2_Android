package com.ivy.sdk.firebase

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.ivy.sdk.base.App

internal object FirebaseInit {


    fun initApp() {
        try {
            FirebaseApp.initializeApp(App.Instance)
        } catch (e: Exception) {

        }
    }
}