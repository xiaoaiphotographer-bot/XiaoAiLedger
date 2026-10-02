package com.xiaoai.ledger.ui.records

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoai.ledger.R
import com.xiaoai.ledger.model.RecordEntry
import com.xiaoai.ledger.model.RecordType

class RecordAdapter(
    private val list: MutableList<RecordEntry> = mutableListOf()
) : RecyclerView.Adapter<RecordAdapter.VH>() {

    fun submit(items: List<RecordEntry>) {
        list.clear(); list.addAll(items); notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: TextView = v.findViewById(R.id.catIcon)
        val category: TextView = v.findViewById(R.id.tvCategory)
        val note: TextView = v.findViewById(R.id.tvNote)
        val amount: TextView = v.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = list[position]
        holder.category.text = r.categoryNameSnapshot
        holder.note.text = r.note
        val sym = if (r.currency == "EUR") "€" else "¥"
        if (r.type == RecordType.EXPENSE.value) {
            holder.amount.text = "-$sym" + String.format(java.util.Locale.US, "%.2f", r.amount)
            holder.amount.setTextColor(holder.itemView.context.getColor(R.color.expense))
        } else {
            holder.amount.text = "+$sym" + String.format(java.util.Locale.US, "%.2f", r.amount)
            holder.amount.setTextColor(holder.itemView.context.getColor(R.color.income))
        }
        holder.icon.text = emojiFor(r.categoryNameSnapshot)
    }

    private fun emojiFor(name: String): String = when (name) {
        "交通" -> "🚌"; "油费" -> "⛽"; "食物" -> "🍚"; "购物" -> "🛍️"
        "住宿" -> "🏨"; "请客" -> "🍻"; "工资" -> "💰"; "摄影" -> "📷"
        "小费" -> "🎁"; "奖金" -> "🧧"; "后期" -> "🎨"
        else -> "📌"
    }
}
