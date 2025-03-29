package com.example.imageapplication.manager

import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.imageapplication.model.DeleteResult
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageManager @Inject constructor(private val context: Context) : IImageManager {

    companion object {
        private const val TAG: String = "ImageManager"
    }

    override fun getImageFlow(rootPath: String): Flow<ImageModel> = flow {
        try {
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
                    // 每次迴圈檢查協程是否取消
                    yield()
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
//                    delay(100)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving images", e)
        }
    }


    /**
     * 針對 Android 11 (API 30) 及以上，生成系統刪除確認的 IntentSender
     */
    override fun requestDeletionIntentSender(images: List<ImageModel>): IntentSender? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                // 將所有圖片 URI 收集成 List，實現一次性刪除
                val uris = images.map { it.uri }
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                pendingIntent.intentSender
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting deletion intent for multiple images", e)
                null
            }
        } else {
            null  // 其他版本使用直接刪除邏輯
        }
    }
    /**
     * 針對 Android 9 ~ Android 10，直接刪除圖片
     */
    override suspend fun deleteImageDirectly(image: ImageModel): DeleteResult {
        return try {
            val rowsDeleted = context.contentResolver.delete(image.uri, null, null)
            if (rowsDeleted > 0) {
                DeleteResult.Success(image)
            } else {
                DeleteResult.Failure(image, Exception("No rows deleted"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
            DeleteResult.Failure(image, e)
        }
    }

}