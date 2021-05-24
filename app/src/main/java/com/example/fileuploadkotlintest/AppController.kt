package com.example.fileuploadkotlintest

import android.app.Application

class AppController: Application() {



    companion object{

        private var mInstance: AppController?=null
        @Synchronized
        fun getInstance(): AppController? {
            return AppController.mInstance
        }
    }



    override fun onCreate() {
        super.onCreate()

        mInstance = this
    }
}