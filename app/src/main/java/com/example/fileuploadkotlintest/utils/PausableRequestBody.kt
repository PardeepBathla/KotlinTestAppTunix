package com.example.fileuploadkotlintest.utils

import android.os.Handler
import android.os.Looper
import com.example.fileuploadkotlintest.UploadProgressCallbacks
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class PausableRequestBody(private val mFile: File, private val callbacks: UploadProgressCallbacks) : RequestBody() {
    var uploadedData: Long = 0
        private set

    override fun contentType(): MediaType? {
        return MediaType.parse("image/*")
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return mFile.length()
    }

    @Throws(IOException::class)
    override fun writeTo(bs: BufferedSink) {
        val fileLength = mFile.length()
        val buffer = ByteArray(BUFFER_SIZE)
        val `in` = FileInputStream(mFile)
        try {
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (`in`.read(buffer).also { read = it } != -1) {
                handler.post(ProgressUpdater(uploadedData, fileLength,callbacks))
                uploadedData += read.toLong()
                bs.write(buffer, 0, read)
            }
        } finally {
            `in`.close()
        }
    }

    companion object {
        private const val BUFFER_SIZE = 2048
    }

    private class ProgressUpdater(
        private val mUploaded: Long,
        private val mTotal: Long,
        val mListener: UploadProgressCallbacks
    ) :
        Runnable {
        override fun run() {
            mListener.onProgressUpdate((100 * mUploaded / mTotal).toInt())
        }

    }



}