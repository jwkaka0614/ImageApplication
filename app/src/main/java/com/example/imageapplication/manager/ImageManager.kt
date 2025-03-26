package com.example.imageapplication.manager

import android.content.ContentUris
import android.content.Context
import android.media.Image
import android.provider.MediaStore
import androidx.compose.ui.graphics.Path
import com.example.imageapplication.model.ImageModel
import java.io.File

class ImageManager(private val context: Context) {


    fun getImageList(path: String): List<ImageModel> {
        val imageList = mutableListOf<ImageModel>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
        )

        val cursor =
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, null
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
                imageList.add(ImageModel(contentUri, name, folderPath))

            }
        }
        return imageList
    }
}