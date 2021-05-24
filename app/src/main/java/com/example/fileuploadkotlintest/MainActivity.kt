package com.example.fileuploadkotlintest

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.fileuploadkotlintest.utils.RunTimePermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), UploadFileCallback {
    private lateinit var testViewModel: TestViewModel
    private var uri: Uri? = null

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                ivPreview.visibility = View.VISIBLE
                ivPreview.setImageURI(result.data?.data) // handle chosen image
                uri = result.data?.data
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViewModel()


        btnSelectImage.setOnClickListener {
            val result: Boolean = RunTimePermissions.checkPermissionGallery(this)
            if (result) openGalleryForImage()
        }

        btnUpload.setOnClickListener {
            Toast.makeText(this, "Upload", Toast.LENGTH_SHORT).show()
            setupRepository();

        }
    }

    private fun setupRepository() {
        testViewModel.makeApiCall(this, uri, this)
        testViewModel.getDataObserver().observe(
            this, Observer {
                Log.d("resp", "response: ")

            }
        )
    }


    private fun setupViewModel() {
        testViewModel = ViewModelProviders.of(this).get(TestViewModel::class.java)
    }

    private fun openGalleryForImage() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startForResult.launch(intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryForImage()
            } else {
                val rationalReadStorage =
                    ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)
                if (!rationalReadStorage)
                //Deny & Don't ask again case
                    Toast.makeText(
                        this,
                        "Allow location permission from settings",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    }

    override fun onSuccess() {
        Log.d("result", "onSuccess: ")
    }

    override fun onError(msg: String) {
        Log.d("result", "onError: ")
    }
}