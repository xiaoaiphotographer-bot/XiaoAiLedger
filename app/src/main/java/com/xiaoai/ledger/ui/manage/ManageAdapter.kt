package com.xiaoai.ledger.ui.manage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoai.ledger.R
import com.xiaoai.ledger.model.Category

class ManageAdapter(
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<ManageAdapter.VH>() {

    private val items = mutableListOf<Category>()

    fun submit(list: List<Category>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: TextView = v.findViewById(R.id.catIcon)
        val name: TextView = v.findViewById(R.id.tvName)
        val status: TextView = v.findViewById(R.id.tvStatus)
        val del: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_category, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.icon.text = emojiFor(c.name)
        holder.name.text = c.name
        if (c.isActive) {
            holder.status.text = ""
            holder.del.visibility = View.VISIBLE
        } else {
            holder.status.text = "(已停用 · 历史账单保留)"
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.text_hint))
            holder.del.visibility = View.GONE
        }
        holder.del.setOnClickListener { onDelete(c) }
    }

    private fun emojiFor(name: String): String = when (name) {
        "交通" -> "🚌"; "油费" -> "⛽"; "食物" -> "🍚"; "购物" -> "🛍️"
        "住宿" -> "🏨"; "请客" -> "🍻"; "工资" -> "💰"; "摄影" -> "📷"
        "小费" -> "🎁"; "奖金" -> "🧧"; "后期" -> "🎨"
        else -> "📌"
    }
}
