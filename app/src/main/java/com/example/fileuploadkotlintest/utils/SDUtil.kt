package com.example.fileuploadkotlintest.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


object SDUtil {
    private val EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE")
    private val SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE")
    private val EMULATED_STORAGE_TARGET =
        System.getenv("EMULATED_STORAGE_TARGET")

    fun getStorageDirectories(context: Context): Array<String> {
        val availableDirectoriesSet: Set<String> = HashSet()
        if (!TextUtils.isEmpty(EMULATED_STORAGE_TARGET)) {
            availableDirectoriesSet.toMutableList().add(emulatedStorageTarget)
        } else {
            availableDirectoriesSet.toMutableList().addAll(getExternalStorage(context))
        }

        val storagesArray = arrayOfNulls<String>(availableDirectoriesSet.size)
        return availableDirectoriesSet.toTypedArray()
    }

    private fun getExternalStorage(context: Context): Set<String> {
        val availableDirectoriesSet: Set<String> = HashSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val files: Array<File> = getExternalFilesDirs(context)
            for (file in files) {
                if (file != null) {
                    val applicationSpecificAbsolutePath: String = file.getAbsolutePath()
                    var rootPath = applicationSpecificAbsolutePath.substring(
                        9, applicationSpecificAbsolutePath.indexOf("Android/data")
                    )
                    rootPath = rootPath.substring(rootPath.indexOf("/storage/") + 1)
                    rootPath = rootPath.substring(0, rootPath.indexOf("/"))
                    if (rootPath != "emulated") {
                        availableDirectoriesSet.toMutableList().add(rootPath)
                    }
                }
            }
        } else {
            if (TextUtils.isEmpty(EXTERNAL_STORAGE)) {
                availableDirectoriesSet.toMutableList().addAll(availablePhysicalPaths)
            } else {
                availableDirectoriesSet.toMutableList().add(EXTERNAL_STORAGE)
            }
        }
        return availableDirectoriesSet
    }

    private val emulatedStorageTarget: String
        private get() {
            var rawStorageId = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val path: String = Environment.getExternalStorageDirectory().getAbsolutePath()
                val folders: Array<String> = path.split(File.separator).toTypedArray()
                val lastSegment = folders[folders.size - 1]
                if (!TextUtils.isEmpty(lastSegment) && TextUtils.isDigitsOnly(lastSegment)) {
                    rawStorageId = lastSegment
                }
            }
            return if (TextUtils.isEmpty(rawStorageId)) {
                EMULATED_STORAGE_TARGET
            } else {
                EMULATED_STORAGE_TARGET + File.separator.toString() + rawStorageId
            }
        }

    private val allSecondaryStorages: Array<String?>
        private get() = if (!TextUtils.isEmpty(SECONDARY_STORAGES)) {
            SECONDARY_STORAGES.split(File.pathSeparator).toTypedArray()
        } else arrayOfNulls(0)

    private val availablePhysicalPaths: List<String>
        private get() {
            val availablePhysicalPaths: List<String> = ArrayList()
            for (physicalPath in KNOWN_PHYSICAL_PATHS) {
                val file = File(physicalPath)
                if (file.exists()) {
                    availablePhysicalPaths.toMutableList().add(physicalPath)
                }
            }
            return availablePhysicalPaths
        }

    private fun getExternalFilesDirs(context: Context): Array<File> {
        return if (Build.VERSION.SDK_INT >= 19) {
            context.getExternalFilesDirs(null)
        } else {
            arrayOf<File>(context.getExternalFilesDir(null)!!)
        }
    }

    @SuppressLint("SdCardPath")
    private val KNOWN_PHYSICAL_PATHS = arrayOf(
        "/storage/sdcard0",
        "/storage/sdcard1",
        "/storage/extsdcard",
        "/storage/sdcard0/external_sdcard",
        "/mnt/extsdcard",
        "/mnt/sdcard/external_sd",
        "/mnt/sdcard/ext_sd",
        "/mnt/external_sd",
        "/mnt/media_rw/sdcard1",
        "/removable/microsd",
        "/mnt/emmc",
        "/storage/external_SD",
        "/storage/ext_sd",
        "/storage/removable/sdcard1",
        "/data/sdext",
        "/data/sdext2",
        "/data/sdext3",
        "/data/sdext4",
        "/sdcard1",
        "/sdcard2",
        "/storage/microsd"
    )
}