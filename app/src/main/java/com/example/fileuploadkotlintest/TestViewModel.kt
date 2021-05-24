package com.example.fileuploadkotlintest

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.example.fileuploadkotlintest.model.TestResponse
import com.ankit.trendinggit.model.api.ApiService
import com.example.fileuploadkotlintest.model.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class TestViewModel : ViewModel() {
    var testLiveData : LiveData<TestResponse> = MutableLiveData()
    var testRepository:TestRepository = TestRepository()

    fun getDataObserver():LiveData<TestResponse>{
        return testLiveData
    }


    fun makeApiCall( context: Context,uri: Uri?,callback: UploadFileCallback){
        CoroutineScope(Dispatchers.IO).async{

         testLiveData =  testRepository.uploadImage(context,uri,callback)
//
        }

    }

}