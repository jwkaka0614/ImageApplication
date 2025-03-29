package com.example.imageapplication.model

import android.content.IntentSender
import android.net.Uri

data class FolderModel(
    val folderPath: String,
    val folderName: String
)

data class FolderGrouping(
    val rootFolder: String,       // 全局根目錄，例如 "/rootPath/"
    val subFolders: List<FolderModel>  // 子資料夾列表
)

data class ImageModel(val uri: Uri, val name: String, val folderPath: String)

// 用於刪除結果的封閉類型，便於上層區分不同錯誤狀況
sealed class DeleteResult {
    data class Success(val image: ImageModel) : DeleteResult()
    data class Failure(val image: ImageModel, val error: Throwable) : DeleteResult()
}