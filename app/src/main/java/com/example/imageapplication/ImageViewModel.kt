package com.example.imageapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.ImageManager
import com.example.imageapplication.model.FolderModel
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImageViewModel @Inject constructor(
    private val imageManager: ImageManager, //圖片
    private val folderRepository: FolderRepository //資料夾
) : ViewModel() {
    private var fetchImagesJob: Job? = null
    private var _currentPath = MutableStateFlow("")
    val currentPath = _currentPath.asStateFlow()
    private val pathStack = ArrayDeque<String>() // 用於追蹤路徑歷史

    private val _imageList = MutableStateFlow<List<ImageModel>>(emptyList())    //當前目錄下所有圖片含子目錄
    val imageList = _imageList.asStateFlow()
    private val _folderList = MutableStateFlow<List<FolderModel>>(emptyList())
    val folderList = _folderList.asStateFlow()
    private val _folderImageList = MutableStateFlow<List<ImageModel>>(emptyList())  //當前資料夾下的圖片
    val folderImageList = _folderImageList.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)    //多選模式
    val isMultiSelectMode = _isMultiSelectMode.asStateFlow()

    private val _selectedImages = MutableStateFlow<Set<ImageModel>>(emptySet()) //被選取圖片清單
    val selectedImages = _selectedImages.asStateFlow()

    fun deleteSelectedImages() {
        viewModelScope.launch {
            _selectedImages.value.forEach { image ->
                imageManager.deleteImage(image)
            }
            _selectedImages.value = emptySet()
        }
    }

    fun deleteImage(image: ImageModel) {
        viewModelScope.launch {
            imageManager.deleteImage(image)

        }
    }

    fun enterMultiSelectMode() {
        _isMultiSelectMode.value = true
    }

    fun exitMultiSelectMode() {
        _isMultiSelectMode.value = false
        _selectedImages.value = emptySet()
    }

    fun toggleImageSelection(image: ImageModel) {
        _selectedImages.update { currentSet ->
            if (currentSet.contains(image)) {
                currentSet - image
            } else {
                currentSet + image
            }
        }
    }

    init {
        // 監聽 currentPath 的變化，調用 fetchImages 新操作
        viewModelScope.launch {
            currentPath.drop(1).collect { newPath ->
                // 根據新的路徑讀取圖片
                fetchImages(newPath)
            }
        }
    }

    //若從Folder Image切回 All Image是否需要重新取?
    //重取會導致Composable刷新而返回top，不重取則無法取得最新資料。目前選擇：重取
    //取得path下所有image 含子資料夾
    fun fetchImages(path: String = "") {
        // 取消先前正在進行的 fetchImages 工作（如果有的話）
        fetchImagesJob?.cancel()

        // 啟動新的協程，並保存該工作
        fetchImagesJob = viewModelScope.launch {
            _imageList.value = emptyList()
            imageManager.getImageFlow(path).collect { image ->
                // 每讀到一筆資料就追加到現有列表中
                _imageList.update { currentList ->
                    currentList + image
                }
            }
        }
    }

    fun fetchFolderPath() {
        val folderGrouping = folderRepository.getFirstLevel(_imageList.value)
        _folderList.value = folderGrouping.subFolders.map { it }
        _currentPath.value = folderGrouping.rootFolder
        fetchCurrentFolderImages(folderGrouping.rootFolder)
    }

    fun fetchCurrentFolderImages(currentFolder: String) {
        // 使用 trimEnd('/') 處理可能多餘的斜線
        val trimmedFolder = currentFolder.trimEnd('/')
        _folderImageList.value = _imageList.value.filter { image ->
            // 只有當 image.folderPath 與當前資料夾路徑完全一致時，才算直接屬於該資料夾
            image.folderPath.trimEnd('/') == trimmedFolder
        }
    }

    fun backPath() {
        if (pathStack.isNotEmpty()) {
            val lastPart = pathStack.removeLast()
            _currentPath.value = _currentPath.value.removeSuffix(lastPart)
            viewModelScope.launch {
                // 如果 fetchImagesJob 存在的話，等待它完成
                fetchImagesJob?.join()

                // 等待完成後進行資料夾資訊更新
                fetchFolderPath()
            }
        }
    }

    fun nextPath(newPath: String) {
        _currentPath.value += newPath
        pathStack.addLast(newPath)
        // 啟動協程等待 fetchImagesJob 完成，再呼叫 fetchFolderPath
        viewModelScope.launch {
            // 如果 fetchImagesJob 存在的話，等待它完成
            fetchImagesJob?.join()

            // 等待完成後進行資料夾資訊更新
            fetchFolderPath()
        }
    }


}