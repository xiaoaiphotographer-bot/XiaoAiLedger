package com.xiaoai.ledger.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.xiaoai.ledger.R
import com.xiaoai.ledger.data.Repository
import com.xiaoai.ledger.databinding.ActivityMainBinding
import com.xiaoai.ledger.model.RecordEntry
import com.xiaoai.ledger.model.RecordType
import com.xiaoai.ledger.ui.calendar.CalendarAdapter
import com.xiaoai.ledger.ui.calendar.DayCellData
import com.xiaoai.ledger.ui.manage.ManageCategoriesActivity
import com.xiaoai.ledger.ui.records.RecordAdapter
import com.xiaoai.ledger.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repo: Repository
    private lateinit var prefs: SharedPreferences
    private var currencyMenuItem: android.view.MenuItem? = null

    private val calendarAdapter = CalendarAdapter { dayMillis ->
        selectedDayMillis = dayMillis
        refreshSelectedDay()
    }
    private val recordAdapter = RecordAdapter()

    private var viewYear: Int = 0
    private var viewMonth0: Int = 0
    private var selectedDayMillis: Long = 0L
    private var currency: String = "CNY"
    private var monthRecords: List<RecordEntry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = Repository.get(this)
        prefs = getSharedPreferences("xiaoai", MODE_PRIVATE)
        currency = prefs.getString("currency", "CNY") ?: "CNY"

        val now = Calendar.getInstance()
        viewYear = now.get(Calendar.YEAR)
        viewMonth0 = now.get(Calendar.MONTH)
        selectedDayMillis = DateUtils.startOfDay(now.timeInMillis)

        setupViews()
        observeMonth()
    }

    private fun setupViews() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_currency -> { switchCurrency(); true }
                R.id.action_stats -> {
                    startActivity(Intent(this, com.xiaoai.ledger.ui.stats.StatsActivity::class.java)); true
                }
                R.id.action_export -> {
                    startActivity(Intent(this, com.xiaoai.ledger.ui.export.ExportActivity::class.java)); true
                }
                R.id.action_manage -> {
                    startActivity(Intent(this, ManageCategoriesActivity::class.java)); true
                }
                else -> false
            }
        }
        invalidateOptionsMenu()

        binding.calendarGrid.layoutManager = GridLayoutManager(this, 7)
        binding.calendarGrid.adapter = calendarAdapter
        binding.calendarGrid.setHasFixedSize(true)
        binding.calendarGrid.isNestedScrollingEnabled = false

        binding.recordsList.layoutManager = LinearLayoutManager(this)
        binding.recordsList.adapter = recordAdapter

        binding.btnPrevMonth.setOnClickListener { shiftMonth(-1) }
        binding.btnNextMonth.setOnClickListener { shiftMonth(+1) }
        binding.tvMonthTitle.setOnClickListener { showMonthPicker() }
        binding.btnToday.setOnClickListener {
            val c = Calendar.getInstance()
            viewYear = c.get(Calendar.YEAR)
            viewMonth0 = c.get(Calendar.MONTH)
            selectedDayMillis = DateUtils.startOfDay(c.timeInMillis)
            observeMonth()
        }

        binding.fabAdd.setOnClickListener {
            val i = Intent(this, com.xiaoai.ledger.ui.add.AddRecordActivity::class.java)
            i.putExtra("dayMillis", selectedDayMillis)
            startActivity(i)
        }
    }

    override fun onPrepareOptionsMenu(menu: android.view.Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        currencyMenuItem = menu.findItem(R.id.action_currency)
        updateCurrencyMenuTitle()
        return true
    }

    private fun updateCurrencyMenuTitle() {
        currencyMenuItem?.title = if (currency == "EUR") "币种：€" else "币种：¥"
    }

    private fun switchCurrency() {
        currency = if (currency == "CNY") "EUR" else "CNY"
        prefs.edit().putString("currency", currency).apply()
        updateCurrencyMenuTitle()
        observeMonth()
        Toast.makeText(this,
            if (currency == "EUR") "已切换到欧元 €" else "已切换到人民币 ¥",
            Toast.LENGTH_SHORT).show()
    }

    private fun shiftMonth(delta: Int) {
        val c = Calendar.getInstance()
        c.set(viewYear, viewMonth0, 1)
        c.add(Calendar.MONTH, delta)
        viewYear = c.get(Calendar.YEAR)
        viewMonth0 = c.get(Calendar.MONTH)
        observeMonth()
    }

    private fun showMonthPicker() {
        val container = FrameLayout(this).apply { setPadding(60, 40, 60, 20) }
        val npYear = NumberPicker(this).apply {
            minValue = 2020; maxValue = 2035
            value = viewYear
            displayedValues = (2020..2035).map { "${it}年" }.toTypedArray()
        }
        val npMonth = NumberPicker(this).apply {
            minValue = 1; maxValue = 12
            value = viewMonth0 + 1
            displayedValues = (1..12).map { "${it}月" }.toTypedArray()
        }
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            addView(npYear, android.widget.LinearLayout.LayoutParams(0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(npMonth, android.widget.LinearLayout.LayoutParams(0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        container.addView(row)
        AlertDialog.Builder(this)
            .setTitle("选择年月")
            .setView(container)
            .setPositiveButton("确定") { _, _ ->
                viewYear = npYear.value
                viewMonth0 = npMonth.value - 1
                observeMonth()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun observeMonth() {
        binding.tvMonthTitle.text = DateUtils.formatMonthTitle(viewYear, viewMonth0)
        val start = DateUtils.startOfMonth(viewYear, viewMonth0)
        val end = DateUtils.endOfMonth(viewYear, viewMonth0)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeRecordsBetween(start, end).collect { list ->
                    monthRecords = list
                    renderCalendar()
                    refreshSelectedDay()
                }
            }
        }
    }

    private fun renderCalendar() {
        val first = Calendar.getInstance()
        first.set(viewYear, viewMonth0, 1)
        val offset = first.get(Calendar.DAY_OF_WEEK) - 1
        val todayMillis = DateUtils.startOfDay(System.currentTimeMillis())

        val expenseByDay = HashMap<Long, Double>()
        val incomeByDay = HashMap<Long, Double>()
        var monthExpense = 0.0
        var monthIncome = 0.0
        monthRecords.filter { it.currency == currency }.forEach { r ->
            val key = DateUtils.startOfDay(r.dateMillis)
            if (r.type == RecordType.EXPENSE.value) {
                expenseByDay[key] = (expenseByDay[key] ?: 0.0) + r.amount
                monthExpense += r.amount
            } else {
                incomeByDay[key] = (incomeByDay[key] ?: 0.0) + r.amount
                monthIncome += r.amount
            }
        }

        val cells = ArrayList<DayCellData>(42)
        val cursor = Calendar.getInstance()
        cursor.set(viewYear, viewMonth0, 1)
        cursor.add(Calendar.DAY_OF_MONTH, -offset)
        repeat(42) {
            val millis = DateUtils.startOfDay(cursor.timeInMillis)
            cells += DayCellData(
                dayMillis = millis,
                isCurrentMonth = cursor.get(Calendar.MONTH) == viewMonth0,
                isToday = millis == todayMillis,
                expenseSum = expenseByDay[millis] ?: 0.0,
                incomeSum = incomeByDay[millis] ?: 0.0
            )
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        calendarAdapter.submit(cells, selectedDayMillis)

        binding.monthExpense.text = DateUtils.money(monthExpense, currency)
        binding.monthIncome.text = DateUtils.money(monthIncome, currency)
        binding.monthBalance.text = DateUtils.money(monthIncome - monthExpense, currency)
    }

    private fun refreshSelectedDay() {
        val start = DateUtils.startOfDay(selectedDayMillis)
        val end = DateUtils.endOfDay(selectedDayMillis)
        binding.selectedDayTitle.text =
            "${DateUtils.formatDayShort(start)}  ${DateUtils.formatDayOfWeek(start)}"
        val items = monthRecords.filter {
            it.currency == currency && it.dateMillis in start until end
        }
        recordAdapter.submit(items)
        if (items.isEmpty()) {
            binding.recordsList.visibility = View.GONE
            binding.emptyHint.visibility = View.VISIBLE
        } else {
            binding.recordsList.visibility = View.VISIBLE
            binding.emptyHint.visibility = View.GONE
        }
    }
}
