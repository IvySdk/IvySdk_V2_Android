package com.ivy.sdk.base.game.archive

interface IArchiveResult {

    fun onSuccess(collection: String, document: String? = null, data: String? = null)

    fun onFailure(collection: String, document: String? = null, reason:String? = null)

}