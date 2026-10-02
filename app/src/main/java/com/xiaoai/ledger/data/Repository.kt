package com.xiaoai.ledger.data

import android.content.Context
import com.xiaoai.ledger.model.Category
import com.xiaoai.ledger.model.RecordEntry
import com.xiaoai.ledger.model.RecordType
import kotlinx.coroutines.flow.Flow

class Repository private constructor(context: Context) {

    private val db = AppDatabase.get(context)
    private val categoryDao = db.categoryDao()
    private val recordDao = db.recordDao()

    suspend fun initIfFirstLaunch() {
        if (categoryDao.count() == 0) {
            SeedData.buildDefaults().forEach { categoryDao.insert(it) }
        }
    }

    fun observeActiveCategories(type: RecordType): Flow<List<Category>> =
        categoryDao.observeActiveByType(type.value)

    fun observeAllCategories(): Flow<List<Category>> = categoryDao.observeAll()

    suspend fun addCategory(name: String, type: RecordType, iconKey: String = "tag") {
        val clean = name.trim()
        if (clean.isEmpty()) return
        categoryDao.insert(Category(name = clean, type = type.value, iconKey = iconKey))
    }

    suspend fun deleteCategory(id: Long): Int {
        val used = categoryDao.countRecordsOf(id)
        return if (used > 0) {
            categoryDao.deactivate(id); 1
        } else {
            categoryDao.physicallyDelete(id); 0
        }
    }

    suspend fun addRecord(
        amount: Double,
        categoryId: Long,
        type: RecordType,
        dateMillis: Long,
        note: String,
        currency: String
    ) {
        val cat = categoryDao.getById(categoryId) ?: return
        recordDao.insert(
            RecordEntry(
                amount = amount,
                categoryId = categoryId,
                categoryNameSnapshot = cat.name,
                type = type.value,
                dateMillis = dateMillis,
                note = note.trim(),
                currency = currency
            )
        )
    }

    fun observeRecordsBetween(start: Long, end: Long): Flow<List<RecordEntry>> =
        recordDao.observeBetween(start, end)

    suspend fun listRecordsBetween(start: Long, end: Long): List<RecordEntry> =
        recordDao.listBetween(start, end)

    suspend fun listAllRecords(): List<RecordEntry> = recordDao.listAll()

    companion object {
        @Volatile private var INSTANCE: Repository? = null
        fun get(context: Context): Repository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Repository(context).also { INSTANCE = it }
            }
    }
}
