package com.ivy.sdk.base.game.archive

import org.json.JSONObject

interface IIArchive : IArchive{
    fun setup(config: JSONObject, debug: Boolean)
}

interface IArchive {

    fun set(userId: String, collection: String, jsonData: String, callback: IArchiveResult)

    fun read(userId: String, collection: String, documentId: String? = null, callback: IArchiveResult)

    fun merge(userId: String, collection: String, jsonData: String, callback: IArchiveResult)

    fun query(userId: String, collection: String, callback: IArchiveResult)

    fun delete(userId: String, collection: String, callback: IArchiveResult)

    fun update(userId: String, collection: String, jsonData: String, callback: IArchiveResult)

    fun snapshot(userId: String, collection: String, documentId: String? = null, callback: IArchiveResult)

}