package com.example.imageapplication.model

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