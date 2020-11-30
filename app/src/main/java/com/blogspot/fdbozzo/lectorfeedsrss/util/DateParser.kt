package com.blogspot.fdbozzo.lectorfeedsrss.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * Date parser
 */
class DateParser {

    companion object {

        /**
         * String to date
         * Convert a string to a date object
         * @param date The date string to convert
         * @param format The string format to parse
         * @return the converted date or null if fail
         */
        fun stringToDate(date: String?, format: String): Date? {
            if (date == null) return null
            val pos = ParsePosition(0)
            return SimpleDateFormat(format, Locale.US).parse(date, pos)
        }

    }

}