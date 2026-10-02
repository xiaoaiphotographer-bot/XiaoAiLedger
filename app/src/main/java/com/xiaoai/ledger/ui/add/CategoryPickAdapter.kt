package com.xiaoai.ledger.ui.add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoai.ledger.R
import com.xiaoai.ledger.model.Category

class CategoryPickAdapter(
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryPickAdapter.VH>() {

    private val items = mutableListOf<Category>()
    private var selectedId: Long = -1

    fun submit(list: List<Category>, defaultSelect: Long = -1) {
        items.clear(); items.addAll(list)
        selectedId = defaultSelect.takeIf { it > 0 } ?: list.firstOrNull()?.id ?: -1
        notifyDataSetChanged()
    }

    fun selected(): Category? = items.firstOrNull { it.id == selectedId }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: TextView = v.findViewById(R.id.catIcon)
        val name: TextView = v.findViewById(R.id.catName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category_pick, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.icon.text = emojiFor(c.name)
        holder.name.text = c.name
        holder.icon.alpha = if (c.id == selectedId) 1f else 0.5f
        holder.icon.setBackgroundColor(
            if (c.id == selectedId)
                holder.itemView.context.getColor(R.color.primary_light)
            else holder.itemView.context.getColor(R.color.divider)
        )
        holder.itemView.setOnClickListener {
            selectedId = c.id
            notifyDataSetChanged()
            onClick(c)
        }
    }

    private fun emojiFor(name: String): String = when (name) {
        "交通" -> "🚌"; "油费" -> "⛽"; "食物" -> "🍚"; "购物" -> "🛍️"
        "住宿" -> "🏨"; "请客" -> "🍻"; "工资" -> "💰"; "摄影" -> "📷"
        "小费" -> "🎁"; "奖金" -> "🧧"; "后期" -> "🎨"
        else -> "📌"
    }
}
