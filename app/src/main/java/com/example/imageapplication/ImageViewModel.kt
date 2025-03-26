package com.example.imageapplication

import androidx.lifecycle.ViewModel
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.ImageManager
import javax.inject.Inject

class ImageViewModel @Inject constructor(
    private val imageManager: ImageManager, //圖片
    private val folderRepository: FolderRepository //資料夾
): ViewModel() {

}