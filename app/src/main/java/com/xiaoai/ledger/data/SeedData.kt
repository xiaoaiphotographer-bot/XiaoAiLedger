package com.xiaoai.ledger.data

import com.xiaoai.ledger.model.Category
import com.xiaoai.ledger.model.RecordType

object SeedData {
    val defaultExpense = listOf(
        "交通" to "transport",
        "油费" to "fuel",
        "食物" to "food",
        "购物" to "shopping",
        "住宿" to "lodging",
        "请客" to "treat"
    )

    val defaultIncome = listOf(
        "工资" to "salary",
        "请客" to "treat_income",
        "摄影" to "camera",
        "小费" to "tip",
        "奖金" to "bonus"
    )

    fun buildDefaults(): List<Category> {
        val list = mutableListOf<Category>()
        var order = 0
        defaultExpense.forEach { (name, icon) ->
            list += Category(name = name, type = RecordType.EXPENSE.value, iconKey = icon, sortOrder = order++)
        }
        order = 0
        defaultIncome.forEach { (name, icon) ->
            list += Category(name = name, type = RecordType.INCOME.value, iconKey = icon, sortOrder = order++)
        }
        return list
    }
}
