package com.ivy.sdk.base.ads


interface IPAMManager {

    fun setupPAMData(data: String?)

    fun getPAM(adType: AdType, price: Double): Any?

}