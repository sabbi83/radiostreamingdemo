package com.harvinder.radiostreamingdemo.api

import com.harvinder.radiostreamingdemo.models.GetPlayNowData
import retrofit2.Response

interface ApiHelper {
    suspend fun getPlayNowData(): Response<GetPlayNowData>

}