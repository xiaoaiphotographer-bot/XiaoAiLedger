package com.xiaoai.ledger.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.xiaoai.ledger.R
import com.xiaoai.ledger.data.Repository
import com.xiaoai.ledger.databinding.ActivityAddRecordBinding
import com.xiaoai.ledger.model.Category
import com.xiaoai.ledger.model.RecordType
import com.xiaoai.ledger.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class AddRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecordBinding
    private lateinit var repo: Repository
    private val pickAdapter = CategoryPickAdapter {}

    private var currentType = RecordType.EXPENSE
    private var selectedDateMillis: Long = 0L
    private var currency: String = "CNY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = Repository.get(this)
        selectedDateMillis = intent.getLongExtra("dayMillis", System.currentTimeMillis())
        selectedDateMillis = DateUtils.startOfDay(selectedDateMillis)
        currency = getSharedPreferences("xiaoai", MODE_PRIVATE).getString("currency", "CNY") ?: "CNY"
        setupTabs()
        setupCategoryGrid()
        setupDatePicker()
        setupCurrencyChips()
        setupSave()
    }

    private fun setupTabs() {
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.expense))
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.income))
        binding.typeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = if (tab.position == 0) RecordType.EXPENSE else RecordType.INCOME
                loadCategories()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupCategoryGrid() {
        binding.categoryGrid.layoutManager = GridLayoutManager(this, 4)
        binding.categoryGrid.adapter = pickAdapter
        loadCategories()
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeActiveCategories(currentType).collect { list ->
                    pickAdapter.submit(list)
                }
            }
        }
    }

    private fun setupDatePicker() {
        renderDate()
        binding.tvDate.setOnClickListener {
            val c = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    c.set(y, m, d)
                    selectedDateMillis = DateUtils.startOfDay(c.timeInMillis)
                    renderDate()
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun renderDate() {
        binding.tvDate.text = DateUtils.formatDateForCsv(selectedDateMillis)
    }

    private fun setupCurrencyChips() {
        if (currency == "EUR") {
            binding.chipEur.isChecked = true
            binding.tvCurrencySymbol.text = "€"
        } else {
            binding.chipCny.isChecked = true
            binding.tvCurrencySymbol.text = "¥"
        }
        binding.currencyGroup.setOnCheckedStateChangeListener { _, _ ->
            currency = if (binding.chipEur.isChecked) "EUR" else "CNY"
            binding.tvCurrencySymbol.text = if (currency == "EUR") "€" else "¥"
        }
    }

    private fun setupSave() {
        binding.btnSave.setOnClickListener {
            val amount = binding.etAmount.text.toString().toDoubleOrNull()
            val cat: Category? = pickAdapter.selected()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "请输入有效金额", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cat == null) {
                Toast.makeText(this, "请选择分类（可先去管理里添加）", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                repo.addRecord(
                    amount = amount,
                    categoryId = cat.id,
                    type = currentType,
                    dateMillis = selectedDateMillis,
                    note = binding.etNote.text.toString(),
                    currency = currency
                )
                finish()
            }
        }
    }
}
