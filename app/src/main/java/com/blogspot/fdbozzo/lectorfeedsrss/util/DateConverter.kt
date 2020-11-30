package com.blogspot.fdbozzo.lectorfeedsrss.util

import androidx.room.TypeConverter
import java.util.*

/**
 * Database type converter
 * Converts date in sql format and conversely
 */
class DateConverter {

    @TypeConverter
    fun dateToTimestamp(date: Date) : Long = date.time

    @TypeConverter
    fun timestampToDate(timestamp: Long) : Date = Date(timestamp)

}