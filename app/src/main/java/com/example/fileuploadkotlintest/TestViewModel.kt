package com.example.fileuploadkotlintest

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ankit.trendinggit.model.TestResponse
import com.example.fileuploadkotlintest.model.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestViewModel : ViewModel() {
    lateinit var testLiveData : MutableLiveData<TestResponse>

    init {
        testLiveData = MutableLiveData()
    }

    fun getDataObserver():MutableLiveData<TestResponse>{
        return testLiveData
    }

    fun makeApiCall(){
        viewModelScope.launch (Dispatchers.IO){

            val response = RetrofitInstance.instance.uploadImage()
//            testLiveData.postValue(response)
        }
    }

}