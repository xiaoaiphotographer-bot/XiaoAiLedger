package com.xiaoai.ledger.ui.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoai.ledger.R

class CalendarAdapter(
    private val onDayClick: (Long) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.VH>() {

    private val cells = mutableListOf<DayCellData>()
    private var selectedMillis: Long = 0L

    fun submit(list: List<DayCellData>, selected: Long) {
        cells.clear(); cells.addAll(list)
        selectedMillis = selected
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val dayNum: TextView = v.findViewById(R.id.tvDayNum)
        val expense: TextView = v.findViewById(R.id.tvExpense)
        val income: TextView = v.findViewById(R.id.tvIncome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_cell, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = cells.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cell = cells[position]
        val cal = java.util.Calendar.getInstance().apply { time = java.util.Date(cell.dayMillis) }
        holder.dayNum.text = cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
        holder.dayNum.setTextColor(
            if (cell.isCurrentMonth) Color.parseColor("#222222")
            else Color.parseColor("#BBBBBB")
        )
        holder.expense.text = if (cell.expenseSum > 0)
            "-" + String.format(java.util.Locale.US, "%.0f", cell.expenseSum) else ""
        holder.income.text = if (cell.incomeSum > 0)
            "+" + String.format(java.util.Locale.US, "%.0f", cell.incomeSum) else ""
        holder.itemView.setBackgroundColor(
            when {
                cell.dayMillis == selectedMillis -> Color.parseColor("#FFE4EC")
                cell.isToday -> Color.parseColor("#FFF3F6")
                else -> Color.TRANSPARENT
            }
        )
        holder.dayNum.setTypeface(null, if (cell.isToday)
            android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        holder.itemView.setOnClickListener { onDayClick(cell.dayMillis) }
    }
}
