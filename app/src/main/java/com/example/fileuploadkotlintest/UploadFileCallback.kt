package com.example.fileuploadkotlintest

public interface UploadFileCallback {

    fun onSuccess()
    fun onError(msg:String)
}