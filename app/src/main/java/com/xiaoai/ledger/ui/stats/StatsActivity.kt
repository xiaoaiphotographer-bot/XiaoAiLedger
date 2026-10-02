package com.xiaoai.ledger.ui.stats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.xiaoai.ledger.R
import com.xiaoai.ledger.data.Repository
import com.xiaoai.ledger.databinding.ActivityStatsBinding
import com.xiaoai.ledger.model.RecordEntry
import com.xiaoai.ledger.model.RecordType
import com.xiaoai.ledger.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var repo: Repository
    private val adapter = StatsAdapter()

    private var type = RecordType.EXPENSE
    private var period = Period.MONTH
    private val cursor: Calendar = Calendar.getInstance()
    private var currency: String = "CNY"

    enum class Period { DAY, WEEK, MONTH, YEAR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = Repository.get(this)
        currency = getSharedPreferences("xiaoai", MODE_PRIVATE).getString("currency", "CNY") ?: "CNY"
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        setupTabs()
        binding.btnPrev.setOnClickListener { shift(-1); refresh() }
        binding.btnNext.setOnClickListener { shift(+1); refresh() }
        refresh()
    }

    private fun setupTabs() {
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.expense))
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.income))
        binding.typeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab) {
                type = if (t.position == 0) RecordType.EXPENSE else RecordType.INCOME
                refresh()
            }
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) {}
        })
        binding.periodTabs.addTab(binding.periodTabs.newTab().setText("日"))
        binding.periodTabs.addTab(binding.periodTabs.newTab().setText("周"))
        binding.periodTabs.addTab(binding.periodTabs.newTab().setText("月"))
        binding.periodTabs.addTab(binding.periodTabs.newTab().setText("年"))
        binding.periodTabs.getTabAt(2)?.select()
        binding.periodTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(t: TabLayout.Tab) {
                period = when (t.position) {
                    0 -> Period.DAY; 1 -> Period.WEEK; 2 -> Period.MONTH; else -> Period.YEAR
                }
                refresh()
            }
            override fun onTabUnselected(t: TabLayout.Tab) {}
            override fun onTabReselected(t: TabLayout.Tab) {}
        })
    }

    private fun shift(delta: Int) {
        when (period) {
            Period.DAY -> cursor.add(Calendar.DAY_OF_MONTH, delta)
            Period.WEEK -> cursor.add(Calendar.WEEK_OF_YEAR, delta)
            Period.MONTH -> cursor.add(Calendar.MONTH, delta)
            Period.YEAR -> cursor.add(Calendar.YEAR, delta)
        }
    }

    private fun range(): Pair<Long, Long> = when (period) {
        Period.DAY -> DateUtils.startOfDay(cursor.timeInMillis) to DateUtils.endOfDay(cursor.timeInMillis)
        Period.WEEK -> DateUtils.startOfWeek(cursor.timeInMillis) to DateUtils.endOfWeek(cursor.timeInMillis)
        Period.MONTH -> DateUtils.startOfMonth(cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH)) to
                DateUtils.endOfMonth(cursor.get(Calendar.YEAR), cursor.get(Calendar.MONTH))
        Period.YEAR -> {
            val y = cursor.get(Calendar.YEAR)
            DateUtils.startOfYear(y) to DateUtils.endOfYear(y)
        }
    }

    private fun periodTitle(): String = when (period) {
        Period.DAY -> SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA).format(cursor.time)
        Period.WEEK -> {
            val start = DateUtils.startOfWeek(cursor.timeInMillis)
            SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(java.util.Date(start)) + " 起这一周"
        }
        Period.MONTH -> "${cursor.get(Calendar.YEAR)}年${cursor.get(Calendar.MONTH) + 1}月"
        Period.YEAR -> "${cursor.get(Calendar.YEAR)}年"
    }

    private fun refresh() {
        binding.tvPeriodTitle.text = periodTitle()
        val (start, end) = range()
        lifecycleScope.launch {
            val all = repo.listRecordsBetween(start, end)
            render(all)
        }
    }

    private fun render(all: List<RecordEntry>) {
        val filtered = all.filter { it.currency == currency && it.type == type.value }
        val byCat = LinkedHashMap<String, Double>()
        filtered.forEach { r ->
            byCat[r.categoryNameSnapshot] = (byCat[r.categoryNameSnapshot] ?: 0.0) + r.amount
        }
        val total = byCat.values.sum()
        val sorted = byCat.entries.sortedByDescending { it.value }
        val sym = if (currency == "EUR") "€" else "¥"
        binding.tvCenterAmount.text = "$sym" + String.format(Locale.US, "%.2f", total)
        binding.tvCenterLabel.text = if (type == RecordType.EXPENSE) "支出合计" else "收入合计"
        val slices = sorted.mapIndexed { idx, e ->
            val pct = if (total > 0) e.value / total * 100 else 0.0
            Slice(
                label = e.key,
                value = e.value,
                color = DonutChartView.PALETTE[idx % DonutChartView.PALETTE.size],
                amountText = "$sym" + String.format(Locale.US, "%.2f", e.value),
                percentText = String.format(Locale.US, "%.1f%%", pct)
            )
        }
        binding.donut.submit(slices)
        adapter.submit(slices)
    }
}
