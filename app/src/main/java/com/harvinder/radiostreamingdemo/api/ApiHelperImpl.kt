package com.harvinder.radiostreamingdemo.api

import com.harvinder.radiostreamingdemo.models.GetPlayNowData
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(
    private val apiService: ApiService
):ApiHelper {
    override suspend fun getPlayNowData(): Response<GetPlayNowData> = apiService.getPlayNow()

}