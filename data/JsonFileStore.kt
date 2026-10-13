package com.CannedF0xy.hideout.data

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type

internal object JsonFileStore {
    private val gson = Gson()

    fun <T> read(context: Context, fileName: String, type: Type): T? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        return try {
            gson.fromJson<T>(file.readText(Charsets.UTF_8), type)
        } catch (e: Exception) {
            null
        }
    }

    fun write(context: Context, fileName: String, value: Any) {
        val target = File(context.filesDir, fileName)
        val temp = File(context.filesDir, "$fileName.tmp")
        temp.writeText(gson.toJson(value), Charsets.UTF_8)
        if (!temp.renameTo(target)) {
            target.writeText(temp.readText(Charsets.UTF_8), Charsets.UTF_8)
            temp.delete()
        }
    }

    fun delete(context: Context, fileName: String) {
        File(context.filesDir, fileName).delete()
    }
}
