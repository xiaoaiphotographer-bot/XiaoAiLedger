package com.xiaoai.ledger.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun startOfDay(millis: Long): Long {
        val c = Calendar.getInstance().apply { time = Date(millis) }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfDay(millis: Long): Long = startOfDay(millis) + 24 * 60 * 60 * 1000L

    fun startOfMonth(year: Int, month0: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month0, 1, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfMonth(year: Int, month0: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month0 + 1, 1, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun startOfWeek(millis: Long): Long {
        val c = Calendar.getInstance().apply { time = Date(millis) }
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfWeek(millis: Long): Long = startOfWeek(millis) + 7 * 24 * 60 * 60 * 1000L

    fun startOfYear(year: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, 0, 1, 0, 0, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun endOfYear(year: Int): Long = startOfYear(year + 1)

    fun formatMonthTitle(year: Int, month0: Int): String = "${year}年${month0 + 1}月"

    fun formatDayOfWeek(millis: Long): String {
        return SimpleDateFormat("EEEE", Locale.CHINA).format(Date(millis))
    }

    fun formatDateForCsv(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

    fun formatDateTimeForCsv(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(millis))

    fun money(amount: Double, currency: String): String {
        val sym = if (currency == "EUR") "€" else "¥"
        return "$sym" + String.format(Locale.US, "%.2f", amount)
    }
}
