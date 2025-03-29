package com.example.imageapplication.manager

import android.content.IntentSender
import com.example.imageapplication.model.DeleteResult
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.flow.Flow

interface IImageManager {

    fun getImageFlow(rootPath: String = ""): Flow<ImageModel>
    fun requestDeletionIntentSender(images: List<ImageModel>): IntentSender?
    suspend fun deleteImageDirectly(image: ImageModel): DeleteResult
}