package com.example.imageapplication.manager

import android.content.Context
import android.provider.MediaStore
import com.example.imageapplication.model.FoldModel
import com.example.imageapplication.model.ImageModel
import java.io.File

class FolderRepository(private val context: Context) {

    fun getAllImageFolders(): List<FoldModel> {
        val folderSet = mutableSetOf<FoldModel>()
        val projection = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )

        cursor?.use {
            val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            while (it.moveToNext()) {
                val path = it.getString(dataColumnIndex)
                val file = File(path)
                val parent = file.parentFile
                val folderPath = parent.absolutePath
                val folderName = file.name
                folderSet.add(FoldModel(folderPath, folderName))
            }
        }
        return folderSet.toList()
    }
}