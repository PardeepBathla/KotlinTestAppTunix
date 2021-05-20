package com.example.fileuploadkotlintest

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.fileuploadkotlintest.utils.RunTimePermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var testViewModel : TestViewModel
    private val REQUEST_CODE: Int = 33

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            ivPreview.visibility = View.VISIBLE
            ivPreview.setImageURI(result.data?.data) // handle chosen image
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnSelectImage.setOnClickListener {

            val result: Boolean = RunTimePermissions.checkPermissionGallery(this)
            if (result) openGalleryForImage()
        }

        btnUpload.setOnClickListener {
            Toast.makeText(this,"Upload",Toast.LENGTH_SHORT).show()
        }
    }




    private fun openGalleryForImage() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startForResult.launch(intent)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty()){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGalleryForImage()
            }else{
                val rationalReadStorage = ActivityCompat.shouldShowRequestPermissionRationale(this,READ_EXTERNAL_STORAGE)
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
}