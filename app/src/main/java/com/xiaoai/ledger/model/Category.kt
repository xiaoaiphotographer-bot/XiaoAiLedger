package com.xiaoai.ledger.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["type", "name"], unique = true)]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: Int,
    val iconKey: String = "tag",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
