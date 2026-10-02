package com.xiaoai.ledger.ui.manage

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.xiaoai.ledger.R
import com.xiaoai.ledger.data.Repository
import com.xiaoai.ledger.databinding.ActivityManageCategoriesBinding
import com.xiaoai.ledger.model.Category
import com.xiaoai.ledger.model.RecordType
import kotlinx.coroutines.launch

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCategoriesBinding
    private lateinit var repo: Repository
    private val adapter = ManageAdapter { cat -> confirmDelete(cat) }
    private var currentType = RecordType.EXPENSE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = Repository.get(this)
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.expense))
        binding.typeTabs.addTab(binding.typeTabs.newTab().setText(R.string.income))
        binding.typeTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentType = if (tab.position == 0) RecordType.EXPENSE else RecordType.INCOME
                load()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        binding.btnAdd.setOnClickListener { showAddDialog() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repo.observeAllCategories().collect { all ->
                    adapter.submit(all.filter { it.type == currentType.value })
                }
            }
        }
    }

    private fun showAddDialog() {
        val et = EditText(this).apply {
            hint = if (currentType == RecordType.EXPENSE) "例如：旅游" else "例如：后期"
        }
        val container = LinearLayout(this).apply {
            setPadding(48, 24, 48, 0)
            addView(et)
        }
        AlertDialog.Builder(this)
            .setTitle(if (currentType == RecordType.EXPENSE) "添加支出分类" else "添加收入分类")
            .setView(container)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = et.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    repo.addCategory(name, currentType)
                    Toast.makeText(this@ManageCategoriesActivity, "已添加：$name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun confirmDelete(cat: Category) {
        lifecycleScope.launch {
            val used = repo.deleteCategory(cat.id)
            when (used) {
                0 -> Toast.makeText(this@ManageCategoriesActivity,
                    "已删除：${cat.name}", Toast.LENGTH_SHORT).show()
                1 -> AlertDialog.Builder(this@ManageCategoriesActivity)
                    .setTitle("已停用该分类")
                    .setMessage("「${cat.name}」已有历史账单记录，因此只停用它而不删除。\n\n以后记账时不再显示这个分类，但过去的账单里仍会保留它。")
                    .setPositiveButton(R.string.sure, null)
                    .show()
            }
        }
    }
}
