package com.xiaoai.ledger.ui.stats

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class Slice(
    val label: String,
    val value: Double,
    val color: Int,
    val amountText: String = "",
    val percentText: String = ""
)

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var slices: List<Slice> = emptyList()
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val oval = RectF()

    companion object {
        val PALETTE = listOf(
            0xFF26A69A.toInt(), 0xFF3F51B5.toInt(), 0xFF8D6E63.toInt(),
            0xFF4CAF50.toInt(), 0xFFE57373.toInt(), 0xFFFFB74D.toInt(),
            0xFFEC407A.toInt(), 0xFF5C6BC0.toInt(), 0xFF9CCC65.toInt(),
            0xFFFFD54F.toInt(), 0xFF7986CB.toInt(), 0xFFA1887F.toInt()
        )
    }

    fun submit(data: List<Slice>) { slices = data; invalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return
        val stroke = Math.min(width, height) * 0.16f
        arcPaint.strokeWidth = stroke
        val pad = stroke / 2f + 4f
        oval.set(pad, pad, width - pad, height - pad)
        val total = slices.sumOf { it.value }
        if (total <= 0) return
        var startAngle = -90f
        val gap = 2f
        slices.forEach { s ->
            val sweep = (s.value / total * 360.0).toFloat()
            arcPaint.color = s.color
            canvas.drawArc(oval, startAngle, sweep - gap, false, arcPaint)
            startAngle += sweep
        }
    }
}
