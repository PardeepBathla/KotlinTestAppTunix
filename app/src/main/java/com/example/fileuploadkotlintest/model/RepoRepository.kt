package com.example.fileuploadkotlintest.model



import com.ankit.trendinggit.model.TestResponse
import com.example.fileuploadkotlintest.model.api.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoRepository {

    // GET repo list
    suspend fun getRepoList(onResult: (isSuccess: Boolean, response: TestResponse?) -> Unit) {

        RetrofitInstance.instance.uploadImage().enqueue(object : Callback<TestResponse> {
            override fun onResponse(call: Call<TestResponse>?, response: Response<TestResponse>?) {
                if (response != null && response.isSuccessful)
                    onResult(true, response.body()!!)
                else
                    onResult(false, null)
            }

            override fun onFailure(call: Call<TestResponse>?, t: Throwable?) {
                onResult(false, null)
            }

        })
    }

    companion object {
        private var INSTANCE: RepoRepository? = null
        fun getInstance() = INSTANCE
                ?: RepoRepository().also {
                    INSTANCE = it
                }
    }
}