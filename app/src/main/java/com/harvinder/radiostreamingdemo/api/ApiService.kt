package com.harvinder.radiostreamingdemo.api

import com.harvinder.radiostreamingdemo.models.GetPlayNowData
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("testapi")
    suspend fun getPlayNow(): Response<GetPlayNowData>
}