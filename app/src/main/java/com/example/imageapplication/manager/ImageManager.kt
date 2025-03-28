package com.example.imageapplication.manager

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Path
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class ImageManager(private val context: Context) {

    companion object {
        private const val TAG: String = "ImageManager"
    }

    fun getImageFlow(rootPath: String = ""): Flow<ImageModel> = flow {

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )

        val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$rootPath%")

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(nameColumn)
                val data = it.getString(dataColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val folderPath = File(data).parent ?: ""
                Log.d(TAG, folderPath)
                emit(ImageModel(contentUri, name, folderPath))
            }
        }
    }

    fun deleteImage(image: ImageModel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 及以上：使用 MediaStore 的刪除請求
            try {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(image.uri))
                // 建立 IntentSenderRequest，並啟動刪除請求
                val intentSenderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "刪除請求失敗", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Android 9 及以下：可直接使用 contentResolver.delete
            try {
                val rowsDeleted = context.contentResolver.delete(image.uri, null, null)
                if (rowsDeleted > 0) {
                    println("圖片已成功刪除: ${image.name}")
                } else {
                    println("圖片刪除失敗: ${image.name}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "刪除時發生例外情況", Toast.LENGTH_SHORT).show()
            }
        }
    }

}