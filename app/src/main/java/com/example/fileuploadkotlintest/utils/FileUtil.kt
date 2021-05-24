package com.example.fileuploadkotlintest.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.fileuploadkotlintest.AppController

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileUtil(private val context: Context) {
    fun createImageUri(): Uri? {
        val contentResolver = context.contentResolver
        val cv = ContentValues()
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        cv.put(MediaStore.Images.Media.TITLE, timeStamp)
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
    }

    /**
     * Create image temp file file.
     *
     * @param filePathDir the file path dir
     * @return the file
     * @throws IOException the io exception
     */
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    fun createImageTempFile(filePathDir: File?): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            filePathDir /* directory */
        )
    }

    companion object {
        private var sSingleton: FileUtil? = null
        private var failReason: String? = null
        fun getInstance(ctx: Context): FileUtil? {
            if (sSingleton == null) {
                synchronized(
                    FileUtil::class.java
                ) { sSingleton = FileUtil(ctx) }
            }
            return sSingleton
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun getRealPathFromURI(
            context: Context,
            uri: Uri
        ): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
// ExternalStorageProvider
                when {
                    isExternalStorageDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        return if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/" + split[1]
                            } else {
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/"
                            }
                        } else {
                            // Some devices does not allow access to the SD Card using the UID, for example /storage/6551-1152/folder/video.mp4
                            // Instead, we first have to get the name of the SD Card, for example /storage/sdcard1/folder/video.mp4

                            // We first have to check if the device allows this access
                            if (File("storage" + "/" + docId.replace(":", "/")).exists()) {
                                return "/storage/" + docId.replace(":", "/")
                            }
                            // If the file is not available, we have to get the name of the SD Card, have a look at SDUtils
                            val availableExternalStorages: Array<String> = SDUtil.getStorageDirectories(context)
                            var root = ""
                            for (s in availableExternalStorages) {
                                root = if (split[1].startsWith("/")) {
                                    s + split[1]
                                } else {
                                    s + "/" + split[1]
                                }
                            }
                            if (root.contains(type)) {
                                "storage" + "/" + docId.replace(":", "/")
                            } else {
                                if (root.startsWith("/storage/") || root.startsWith("storage/")) {
                                    root
                                } else if (root.startsWith("/")) {
                                    "/storage$root"
                                } else {
                                    "/storage/$root"
                                }
                            }
                        }
                    }
                    isDownloadsDocument(uri) -> {
                        val fileName = getFilePath(context, uri)
                        if (fileName != null) {
                            return Environment.getExternalStorageDirectory()
                                .toString() + "/Download/" + fileName
                        }
                        var id = DocumentsContract.getDocumentId(uri)
                        if (id.startsWith("raw:")) {
                            id = id.replaceFirst("raw:".toRegex(), "")
                            val file = File(id)
                            if (file.exists()) return id
                        }
                        if (id.startsWith("raw%3A%2F")) {
                            id = id.replaceFirst("raw%3A%2F".toRegex(), "")
                            val file = File(id)
                            if (file.exists()) return id
                        }
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong()
                        )
                        return getDataColumn(context, contentUri, null, null)
                    }
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        if ("image" == type) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == type) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == type) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(
                            split[1]
                        )
                        return getDataColumn(
                            context,
                            contentUri,
                            selection,
                            selectionArgs
                        )
                    }
                }
            } else return if ("content".equals(uri.scheme, ignoreCase = true)) {
// Return the remote address
                if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                uri.path
            } else getRealPathFromURIDB(uri)
            return null
        }

        private fun getFilePath(
            context: Context,
            uri: Uri
        ): String? {
            var cursor: Cursor? = null
            val projection =
                arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
            try {
                cursor = context.contentResolver.query(
                    uri, projection, null, null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    return cursor.getString(index)
                }
            } catch (e: Exception) {
                failReason = e.message
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * Gets real path from uri.
         *
         * @param contentUri the content uri
         * @return the real path from uri
         */
        private fun getRealPathFromURIDB(contentUri: Uri): String? {
            val cursor: Cursor? = AppController.getInstance()?.run {
                contentResolver.query(contentUri, null, null, null, null)
            }
            return if (cursor == null) {
                contentUri.path
            } else {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val realPath = cursor.getString(index)
                cursor.close()
                realPath
            }
        }

        /**
         * Gets data column.
         *
         * @param uri           the uri
         * @param selection     the selection
         * @param selectionArgs the selection args
         * @return the data column
         */
        private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                column
            )
            try {
                cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * / **
         * Is external storage document boolean.
         *
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * Is downloads document boolean.
         *
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * Is media document boolean.
         *
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        private fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * Is google photos uri boolean.
         *
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        private fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }

}