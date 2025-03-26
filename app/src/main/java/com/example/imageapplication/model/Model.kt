package com.example.imageapplication.model

import android.net.Uri

data class FoldModel(val folderPath: String, val folderName: String)

data class ImageModel(val uri: Uri, val name: String, val folderPath: String)