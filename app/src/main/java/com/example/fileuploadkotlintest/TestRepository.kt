package com.example.fileuploadkotlintest

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.fileuploadkotlintest.model.TestResponse
import com.ankit.trendinggit.model.api.ApiService
import com.example.fileuploadkotlintest.model.api.RetrofitInstance
import com.example.fileuploadkotlintest.utils.FileUtil.Companion.getRealPathFromURI
import com.example.fileuploadkotlintest.utils.PausableRequestBody
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.log

class TestRepository : UploadProgressCallbacks {


    var apiService: ApiService = RetrofitInstance.retrofit.create(ApiService::class.java)
    private lateinit var timer: Timer
    private var context: Context? = null
    lateinit var call: Call<TestResponse>
    lateinit var callback: UploadFileCallback
    lateinit var newsData: MutableLiveData<TestResponse>


    fun uploadImage(
        context: Context,
        uri: Uri?,
        callback: UploadFileCallback
    ): MutableLiveData<TestResponse> {



        this.context = context
        newsData = MutableLiveData<TestResponse>()



        /* var mProgress: Dialog? = null
         mProgress = getProgressDialog(context)*/

        val file = File(uri?.let {
            getRealPathFromURI(context, it)

        })

        val fileBody = PausableRequestBody(file, this)
        val requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), file)

        val imageBody = MultipartBody.Part.createFormData(
            "image[0]",
            file.name,
            fileBody
        )



        this.callback = callback
        call = apiService.uploadImage(imageBody)

//        mProgress?.show()
        enqueueCall(callback, newsData)

        setUpTimer()
        return newsData
    }

    private fun enqueueCall(
        callback: UploadFileCallback,
        newsData: MutableLiveData<TestResponse>
    ) {
        call.enqueue(object : Callback<TestResponse?> {
            override fun onResponse(call: Call<TestResponse?>, response: Response<TestResponse?>) {
                //                mProgress?.dismiss()


                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    newsData.value = TestResponse("failed")
                }
            }

            override fun onFailure(call: Call<TestResponse?>, t: Throwable) {
                //                mProgress?.dismiss()
                newsData.value = TestResponse("failed")

                t.message?.let { callback.onError(it) }
            }
        })
    }

    private fun setUpTimer() {
        timer = Timer()
        timer.scheduleAtFixedRate(

            timerTask {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context?.let {
                        if (checkSpeed(it)) {
                            Log.d("TAG", "setUpTimer: ")
                            if (call.isCanceled) {
                                enqueueCall(callback, newsData)
                            }
                        } else {
                            call.cancel()
                        }
                    }

                } else {
                    TODO("VERSION.SDK_INT < M")
                }


            }, 2000, 2
        )
    }

    fun getProgressDialog(mActivity: Context?): Dialog? {
        val mDialog = Dialog(mActivity!!)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.setCancelable(false)
        mDialog.setContentView(R.layout.progress_layout)
        mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return mDialog
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onProgressUpdate(percentage: Int) {
        Log.d("onProgressUpdate: ", "$percentage")
    }

    override fun onError() {
        timer.cancel()
    }

    override fun onFinish() {
        timer.cancel()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun checkSpeed(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetwork

        val nc = cm.getNetworkCapabilities(cm.activeNetwork)
        val downSpeed = nc?.linkDownstreamBandwidthKbps
        val upSpeed = nc?.linkUpstreamBandwidthKbps

        return upSpeed!! > 1000

    }

}