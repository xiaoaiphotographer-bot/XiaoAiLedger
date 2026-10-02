package com.xiaoai.ledger.ui.calendar

data class DayCellData(
    val dayMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val expenseSum: Double = 0.0,
    val incomeSum: Double = 0.0
)
