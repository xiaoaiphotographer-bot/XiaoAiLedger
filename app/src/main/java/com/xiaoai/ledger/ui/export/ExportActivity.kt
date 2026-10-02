package com.xiaoai.ledger.ui.export

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xiaoai.ledger.data.Repository
import com.xiaoai.ledger.databinding.ActivityExportBinding
import com.xiaoai.ledger.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = Repository.get(this)
        binding.btnDay.setOnClickListener { doExport("今日") { rangeForDay() } }
        binding.btnWeek.setOnClickListener { doExport("本周") { rangeForWeek() } }
        binding.btnMonth.setOnClickListener { doExport("本月") { rangeForMonth() } }
        binding.btnYear.setOnClickListener { doExport("今年") { rangeForYear() } }
        binding.btnAll.setOnClickListener { doExport("全部") { rangeForAll() } }
    }

    private data class Range(val start: Long, val end: Long)

    private fun rangeForDay(): Range {
        val now = System.currentTimeMillis()
        return Range(DateUtils.startOfDay(now), DateUtils.endOfDay(now))
    }
    private fun rangeForWeek(): Range {
        val now = System.currentTimeMillis()
        return Range(DateUtils.startOfWeek(now), DateUtils.endOfWeek(now))
    }
    private fun rangeForMonth(): Range {
        val c = Calendar.getInstance()
        return Range(DateUtils.startOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH)),
            DateUtils.endOfMonth(c.get(Calendar.YEAR), c.get(Calendar.MONTH)))
    }
    private fun rangeForYear(): Range {
        val y = Calendar.getInstance().get(Calendar.YEAR)
        return Range(DateUtils.startOfYear(y), DateUtils.endOfYear(y))
    }
    private fun rangeForAll(): Range = Range(0L, Long.MAX_VALUE)

    private fun doExport(title: String, rangeProvider: () -> Range) {
        lifecycleScope.launch {
            binding.tvStatus.text = "正在导出..."
            val r = rangeProvider()
            val list = withContext(Dispatchers.IO) {
                if (r.start == 0L) repo.listAllRecords()
                else repo.listRecordsBetween(r.start, r.end)
            }
            if (list.isEmpty()) {
                binding.tvStatus.text = "该时间段内没有账单，未生成文件。"
                Toast.makeText(this@ExportActivity, "没有可导出的数据", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val uri = withContext(Dispatchers.IO) {
                ExportHelper.exportCsv(this@ExportActivity, title, list)
            }
            binding.tvStatus.text = "已导出 ${list.size} 条记录到 Download/小埃记账本/"
            ExportHelper.share(this@ExportActivity, uri)
        }
    }
}
