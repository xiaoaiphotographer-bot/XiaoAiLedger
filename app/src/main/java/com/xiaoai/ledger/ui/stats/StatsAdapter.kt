package com.xiaoai.ledger.ui.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoai.ledger.R

class StatsAdapter : RecyclerView.Adapter<StatsAdapter.VH>() {

    private val items = mutableListOf<Slice>()

    fun submit(list: List<Slice>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val dot: View = v.findViewById(R.id.colorDot)
        val name: TextView = v.findViewById(R.id.tvName)
        val amount: TextView = v.findViewById(R.id.tvAmount)
        val percent: TextView = v.findViewById(R.id.tvPercent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stats_row, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.dot.setBackgroundColor(s.color)
        holder.name.text = s.label
        holder.amount.text = s.amountText
        holder.percent.text = s.percentText
    }
}
