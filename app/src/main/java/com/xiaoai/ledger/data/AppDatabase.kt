package com.xiaoai.ledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xiaoai.ledger.model.Category
import com.xiaoai.ledger.model.RecordEntry

@Database(
    entities = [Category::class, RecordEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xiaoai_ledger.db"
                ).build().also { INSTANCE = it }
            }
    }
}
