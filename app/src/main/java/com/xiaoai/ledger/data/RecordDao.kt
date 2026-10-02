package com.xiaoai.ledger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.xiaoai.ledger.model.RecordEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: RecordEntry): Long

    @Query("SELECT * FROM records WHERE dateMillis >= :start AND dateMillis < :end ORDER BY dateMillis DESC, id DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<RecordEntry>>

    @Query("SELECT * FROM records WHERE dateMillis >= :start AND dateMillis < :end ORDER BY dateMillis DESC, id DESC")
    suspend fun listBetween(start: Long, end: Long): List<RecordEntry>

    @Query("SELECT * FROM records ORDER BY dateMillis DESC, id DESC")
    suspend fun listAll(): List<RecordEntry>

    @Query("SELECT COUNT(*) FROM records")
    suspend fun count(): Int
}
