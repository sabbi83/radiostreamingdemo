package com.harvinder.radiostreamingdemo.dao

import androidx.room.*
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayListDao {
    @Transaction
    @Query("SELECT * FROM users_table")
    fun getUserDetails() : Flow<List<GetPlayNowDataItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertList(platList: GetPlayNowDataItem): Long

    @Delete
    suspend fun deleteAllUsersDetails(user: GetPlayNowDataItem)

    @Query("DELETE FROM users_table")
    suspend fun deleteAll()
}