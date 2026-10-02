package com.xiaoai.ledger.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "records",
    indices = [Index("dateMillis"), Index("categoryId")]
)
data class RecordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val categoryNameSnapshot: String,
    val type: Int,
    val dateMillis: Long,
    val note: String = "",
    val currency: String = "CNY",
    val createdAt: Long = System.currentTimeMillis()
)
