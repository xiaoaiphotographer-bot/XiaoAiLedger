package com.xiaoai.ledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.xiaoai.ledger.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE type = :type AND isActive = 1 ORDER BY sortOrder ASC, id ASC")
    fun observeActiveByType(type: Int): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY type ASC, sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Query("UPDATE categories SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun physicallyDelete(id: Long)

    @Query("SELECT COUNT(*) FROM records WHERE categoryId = :categoryId")
    suspend fun countRecordsOf(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
