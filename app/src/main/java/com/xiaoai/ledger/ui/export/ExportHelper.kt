package com.xiaoai.ledger.ui.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.xiaoai.ledger.model.RecordEntry
import com.xiaoai.ledger.model.RecordType
import com.xiaoai.ledger.util.DateUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private const val DIR_NAME = "小埃记账本"

    fun exportCsv(context: Context, title: String, records: List<RecordEntry>): Uri {
        val sb = StringBuilder()
        sb.append('﻿')
        sb.append("日期,类型,分类,金额,币种,备注,记录时间\n")
        records.forEach { r ->
            val typeStr = if (r.type == RecordType.EXPENSE.value) "支出" else "收入"
            val amount = String.format(Locale.US, "%.2f", r.amount)
            val date = DateUtils.formatDateForCsv(r.dateMillis)
            val time = DateUtils.formatDateTimeForCsv(r.createdAt)
            val note = escape(r.note)
            sb.append("$date,$typeStr,${r.categoryNameSnapshot},$amount,${r.currency},$note,$time\n")
        }
        val fileName = "小埃记账本_${title}_" +
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".csv"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(context, fileName, sb.toString())
        } else {
            writeViaLegacyFile(context, fileName, sb.toString())
        }
    }

    private fun escape(s: String): String {
        if (s.isEmpty()) return ""
        if (s.contains(",") || s.contains("\""") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\""
        }
        return s
    }

    private fun writeViaMediaStore(context: Context, fileName: String, content: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$DIR_NAME")
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("无法创建文件")
        resolver.openOutputStream(uri).use { os ->
            os!!.write(content.toByteArray(Charsets.UTF_8))
        }
        return uri
    }

    @Suppress("DEPRECATION")
    private fun writeViaLegacyFile(context: Context, fileName: String, content: String): Uri {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DIR_NAME
        ).apply { mkdirs() }
        val f = File(dir, fileName)
        FileOutputStream(f).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
    }

    fun share(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享账单 CSV"))
    }
}
