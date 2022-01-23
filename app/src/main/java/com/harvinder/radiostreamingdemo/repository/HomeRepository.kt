package com.harvinder.radiostreamingdemo.repository

import com.harvinder.radiostreamingdemo.api.ApiHelper
import com.harvinder.radiostreamingdemo.dao.PlayListDao
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiHelper: ApiHelper,private val playListDao: PlayListDao
)
{
suspend fun getPlayList() =apiHelper.getPlayNowData()
    suspend fun createUserRecords(getPlayNowDataItem: GetPlayNowDataItem) : Long {
        return playListDao.insertList(getPlayNowDataItem)
    }
    //get single user details.
    val getPlayDetails: Flow<List<GetPlayNowDataItem>> get() =  playListDao.getUserDetails()

    //delete Record
    suspend fun deleteAllRecord(getPlayNowDataItem: GetPlayNowDataItem) {
        playListDao.deleteAllUsersDetails(getPlayNowDataItem)
    }

    //Delete Data

    suspend fun deleteAll(){
        playListDao.deleteAll()
    }
}