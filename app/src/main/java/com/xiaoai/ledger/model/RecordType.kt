package com.xiaoai.ledger.model

enum class RecordType(val value: Int) {
    EXPENSE(0),
    INCOME(1);

    companion object {
        fun fromInt(v: Int): RecordType = if (v == 1) INCOME else EXPENSE
    }
}
