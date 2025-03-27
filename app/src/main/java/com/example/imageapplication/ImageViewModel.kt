package com.example.imageapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.ImageManager
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageViewModel @Inject constructor(
    private val imageManager: ImageManager, //圖片
    private val folderRepository: FolderRepository //資料夾
) : ViewModel() {

    private val _imageList = MutableStateFlow<List<ImageModel>>(emptyList())
    val imageList = _imageList.asStateFlow()
//    init {
//        imageManager.getImageList()
//    }

    fun fetchImages() {
        viewModelScope.launch {
            imageManager.getImageFlow().collect { image ->
                // 每讀到一筆資料就追加到現有列表中
                _imageList.update { currentList ->
                    currentList + image
                }
            }
        }
    }
}