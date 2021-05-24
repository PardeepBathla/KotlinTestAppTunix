package com.example.fileuploadkotlintest

interface UploadProgressCallbacks {
        fun onProgressUpdate(percentage: Int)
        fun onError()
        fun onFinish()
    }