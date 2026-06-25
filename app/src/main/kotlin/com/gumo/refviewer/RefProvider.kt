package com.gumo.refviewer

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import java.io.File

class RefProvider : ContentProvider() {
    override fun onCreate() = true
    override fun query(uri: Uri, p: Array<String>?, s: String?, sa: Array<String>?, so: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, sa: Array<String>?) = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, sa: Array<String>?) = 0

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        return when (method) {
            "list" -> Bundle().apply {
                val folder = File("/storage/emulated/0/Documents/references")
                val files = folder.listFiles()?.map { it.name } ?: emptyList()
                putStringArray("files", files.toTypedArray())
            }
            else -> null
        }
    }
}
