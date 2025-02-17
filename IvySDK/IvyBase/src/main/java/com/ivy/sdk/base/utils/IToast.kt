package com.ivy.sdk.base.utils

import android.widget.Toast
import com.ivy.sdk.base.App

class IToast {

    companion object {

        var debug: Boolean = false

        fun toast(msg: String) {
            if (debug) {
                ActivityUtil.Instance.activity?.let {
                    Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun toastAnyway(msg: String) {
            ActivityUtil.Instance.activity?.let {
                Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
            }
        }


    }

}