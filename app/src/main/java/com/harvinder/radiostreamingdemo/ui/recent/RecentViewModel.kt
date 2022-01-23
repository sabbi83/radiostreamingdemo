package com.harvinder.radiostreamingdemo.ui.recent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import com.harvinder.radiostreamingdemo.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class RecentViewModel  @Inject constructor(private val homeRepository: HomeRepository) : ViewModel() {

    /**
     * Retrieve user details
     */
    //check if song is liked
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
}