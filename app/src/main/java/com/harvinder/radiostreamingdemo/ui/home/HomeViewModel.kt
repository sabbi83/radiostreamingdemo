package com.harvinder.radiostreamingdemo.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvinder.radiostreamingdemo.models.GetPlayNowData
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import com.harvinder.radiostreamingdemo.others.Resource
import com.harvinder.radiostreamingdemo.repository.HomeRepository
import com.harvinder.radiostreamingdemo.utils.Common
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel  @Inject constructor(private val homeRepository: HomeRepository): ViewModel(){

    private val getPlayNowData = MutableLiveData<Resource<GetPlayNowData>>()
    private  val getPlayNowDataItem=MutableLiveData<GetPlayNowDataItem>()

    val res : LiveData<Resource<GetPlayNowData>>
        get() = getPlayNowData

    init {
        getCureentList()
    }
    //insert  details to room database
    fun insertUserDetails(getPlayNowDataItem:GetPlayNowDataItem){
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.createUserRecords(getPlayNowDataItem)
        }
    }

    /**
     * Delete all Record
     */
    fun doDeleteAllRecord(getPlayNowDataItem:GetPlayNowDataItem){
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.deleteAllRecord(getPlayNowDataItem)
        }
    }

    /**
     *
     * Delete all data from table
     */

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.deleteAll()
        }
    }


    /**
     *
     *
     * Retrieve  details
     */
    private val _userDetails = MutableStateFlow<List<GetPlayNowDataItem>>(emptyList())
    val userDetails : StateFlow<List<GetPlayNowDataItem>> =  _userDetails

    fun doGetUserDetails(){
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.getPlayDetails
                .collect {
                     _userDetails.value=it
                   }

        }
    }
    private fun getCureentList()  = viewModelScope.launch {

        getPlayNowData.postValue(Resource.loading(null))

        homeRepository.getPlayList().let {
            if (it.isSuccessful){
                getPlayNowData.postValue(Resource.success(it.body()))

            }else{
                getPlayNowData.postValue(Resource.error(it.errorBody().toString(), null))
            }
        }
    }
}