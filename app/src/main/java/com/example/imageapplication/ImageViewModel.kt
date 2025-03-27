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
    private var _currentPath = MutableStateFlow("")
    private val currentPath = _currentPath.asStateFlow()
    private val _imageList = MutableStateFlow<List<ImageModel>>(emptyList())
    val imageList = _imageList.asStateFlow()
    private val _folderList = MutableStateFlow<List<String>>(emptyList())
    val folderList = _folderList.asStateFlow()

    init {
        // 監聽 currentPath 的變化，調用 fetchImages 新操作
        viewModelScope.launch {
            currentPath.collect { newPath ->
                // 在 currentPath 變化後，先清空目前圖片列表（可選）
                _imageList.value = emptyList()
                // 根據新的路徑讀取圖片
                fetchImages(newPath)
            }
        }
    }

    fun fetchImages(path: String = "") {
        viewModelScope.launch {
            imageManager.getImageFlow().collect { image ->
                // 每讀到一筆資料就追加到現有列表中
                _imageList.update { currentList ->
                    currentList + image
                }
            }
        }
    }
    fun fetchFolderPath() {
        val folderGrouping = folderRepository.getFirstLevel(_imageList.value)
        _folderList.value = folderGrouping.subFolders.map { it.folderName }
        _currentPath.value = folderGrouping.rootFolder
    }


}