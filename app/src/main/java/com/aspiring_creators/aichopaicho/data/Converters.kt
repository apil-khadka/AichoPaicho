package com.aspiring_creators.aichopaicho.data

import android.net.Uri
import androidx.room.TypeConverter
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    @TypeConverter
    fun fromUri(uri : Uri?): String? {
        return uri?.toString() ?: ""
    }

    @TypeConverter
    fun stringToUri(value: String?): Uri?{
        return value?.toUri()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(value: List<String?>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String?> {
        val listType = object : TypeToken<List<String?>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

}