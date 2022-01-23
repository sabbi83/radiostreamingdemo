package com.harvinder.radiostreamingdemo.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.harvinder.radiostreamingdemo.di.ApplicationScope
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [GetPlayNowDataItem::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlayListDao
    class Callback @Inject constructor(private val songDatabase: Provider<AppDatabase>, @ApplicationScope private val applicationScope: CoroutineScope) : androidx.room.RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = songDatabase.get().playlistDao()
            applicationScope.launch {

            }
        }
    }


}