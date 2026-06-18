package com.peanutbutter1001.qron.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY scannedAtTimestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history_table WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Long): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: HistoryEntity): Long

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun deleteHistory(id: Long): Int

    @Query("DELETE FROM history_table")
    suspend fun clearAllHistory(): Int
}
