package com.ivy.sdk.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ivy.sdk.base.game.archive.IArchiveResult
import com.ivy.sdk.base.game.archive.IIArchive
import com.ivy.sdk.base.utils.ILog
import org.json.JSONArray
import org.json.JSONObject


open class FirestoreImpl : IIArchive {

    companion object {
        const val TAG = "Firestore"
    }

    private var db: FirebaseFirestore? = null

    override fun setup(config: JSONObject, debug: Boolean) {
        try {
            FirebaseFirestore.setLoggingEnabled(debug)
            val database = config.optString("database")
            db = if (database.isNullOrEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                FirebaseFirestore.getInstance(database)
            }
        } catch (e: Exception) {
            ILog.e(TAG, "parse database config err:${e.message}")
            ILog.e(TAG, "use default firestore instance")
            db = FirebaseFirestore.getInstance()
        }
    }

    private fun jsonToMap(jsonData: String): Map<String, Any>? {
        try {
            val json = JSONObject(jsonData)
            val map = mutableMapOf<String, Any>()
            json.keys().forEach {
                json.opt(it)?.let { value ->
                    map[it] = value
                }
            }
        } catch (e: Exception) {
            ILog.e(TAG, "format data err:${e.message}")
            ILog.e(TAG, "err data :$jsonData")
        }
        return null
    }

    override fun set(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                jsonToMap(jsonData)?.let { data ->
                    impl.collection(collection).document(userId).set(data).addOnSuccessListener {
                        callback.onSuccess(collection)
                    }.addOnFailureListener { e ->
                        ILog.e(TAG, "set data failed:${e.message}")
                        ILog.e(TAG, "data:$jsonData")
                        callback.onFailure(collection)
                    }
                }

            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                ILog.e(TAG, "data:$jsonData")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun read(userId: String, collection: String, documentId: String?, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                val customDocumentId = if (documentId.isNullOrEmpty()) userId else documentId
                impl.collection(collection).document(customDocumentId).get().addOnSuccessListener { document ->
                    if (document == null || !document.exists()) {
                        ILog.e(TAG, "$collection;$documentId data does not exists")
                        callback.onSuccess(collection, customDocumentId, "{}")
                        return@addOnSuccessListener
                    }
                    document.data?.let { data ->
                        val json = JSONObject(data)
                        callback.onSuccess(collection, documentId, json.toString())
                    } ?: run {
                        ILog.e(TAG, "$collection;$documentId data is null")
                        callback.onSuccess(collection, documentId, "{}")
                    }
                }.addOnFailureListener { e ->
                    ILog.e(TAG, "set data failed:${e.message}")
                    callback.onFailure(collection)
                }
            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun merge(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                jsonToMap(jsonData)?.let { data ->
                    impl.collection(collection).document(userId).set(data, SetOptions.merge()).addOnSuccessListener {
                        callback.onSuccess(collection)
                    }.addOnFailureListener { e ->
                        ILog.e(TAG, "set data failed:${e.message}")
                        ILog.e(TAG, "data:$jsonData")
                        callback.onFailure(collection)
                    }
                }

            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                ILog.e(TAG, "data:$jsonData")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun query(userId: String, collection: String, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                impl.collection(collection).get().addOnSuccessListener { document ->
                    if (document == null || document.isEmpty) {
                        callback.onSuccess(collection, null, "[]")
                        return@addOnSuccessListener
                    }
                    val json = JSONArray()
                    document.documents.forEach {
                        if (it.exists() && it.data != null) {
                            json.put(JSONObject(it.data))
                        }
                    }
                    callback.onSuccess(collection, null, json.toString())
                }.addOnFailureListener { e ->
                    ILog.e(TAG, "set data failed:${e.message}")
                    callback.onFailure(collection)
                }
            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun delete(userId: String, collection: String, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                impl.collection(collection).document(userId).delete().addOnSuccessListener {
                    callback.onSuccess(collection)
                }.addOnFailureListener { e ->
                    ILog.e(TAG, "set data failed:${e.message}")
                    callback.onFailure(collection)
                }
            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun update(userId: String, collection: String, jsonData: String, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                jsonToMap(jsonData)?.let { data ->
                    impl.collection(collection).document(userId).update(data).addOnSuccessListener {
                        callback.onSuccess(collection)
                    }.addOnFailureListener { e ->
                        ILog.e(TAG, "set data failed:${e.message}")
                        ILog.e(TAG, "data:$jsonData")
                        callback.onFailure(collection)
                    }
                }

            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                ILog.e(TAG, "data:$jsonData")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }

    override fun snapshot(userId: String, collection: String, documentId: String?, callback: IArchiveResult) {
        db?.let { impl ->
            try {
                val customDocumentId = if (documentId.isNullOrEmpty()) userId else documentId
                impl.collection(collection).document(customDocumentId).addSnapshotListener { value, error ->
                    if (error != null) {
                        ILog.e(TAG, "snapshot err:${error.code};${error.message}")
                        callback.onFailure(collection)
                        return@addSnapshotListener
                    }
                    if (value != null && value.exists()) {
                        value.data?.let { data ->
                            callback.onSuccess(collection, customDocumentId, JSONObject(data).toString())
                        } ?: run {
                            ILog.e(TAG, "snapshot data invalid")
                            callback.onFailure(collection)
                        }
                    } else {
                        ILog.e(TAG, "snapshot invalid")
                        callback.onFailure(collection)
                    }
                }
            } catch (e: Exception) {
                ILog.e(TAG, "set data failed:${e.message}")
                callback.onFailure(collection)
            }
        } ?: {
            ILog.w(TAG, "invalid firestore instance")
            callback.onFailure(collection)
        }
    }


}