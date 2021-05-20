package com.example.fileuploadkotlintest.utils

import android.Manifest
import android.R
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class RunTimePermissions {


    companion object {

        const val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        fun checkPermissionGallery(context: Context): Boolean {
            val currentAPIVersion = Build.VERSION.SDK_INT
            return if (currentAPIVersion >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((context as Activity?)!!, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        val alertBuilder = AlertDialog.Builder(context)
                        alertBuilder.setCancelable(true)
                        alertBuilder.setTitle("Permission necessary")
                        alertBuilder.setMessage("Read External storage permission permission is necessary")
                        alertBuilder.setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                            ActivityCompat.requestPermissions(
                                (context as Activity?)!!,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE
                                ),
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                            )
                        }
                        val alert = alertBuilder.create()
                        alert.show()
                    } else {
                        ActivityCompat.requestPermissions(
                            (context as Activity?)!!,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                        )
                    }
                    false
                } else {
                    true
                }
            } else {
                true
            }
        }
    }

}