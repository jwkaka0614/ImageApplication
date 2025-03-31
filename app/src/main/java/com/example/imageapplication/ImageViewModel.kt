package com.example.imageapplication

import android.content.IntentSender
import android.nfc.Tag
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imageapplication.manager.FolderRepository
import com.example.imageapplication.manager.ImageManager
import com.example.imageapplication.model.DeleteResult
import com.example.imageapplication.model.FolderModel
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


// 刪除事件使用 sealed class 定義
sealed class DeletionEvent {
    data class ShowDeletionConfirmation(val images: List<ImageModel>, val intentSender: IntentSender) : DeletionEvent()
    data class DeletionResult(val images: List<ImageModel>, val success: Boolean) : DeletionEvent()
}

class ImageViewModel @Inject constructor(
    private val imageManager: ImageManager, //圖片
    private val folderRepository: FolderRepository //資料夾
) : ViewModel() {
    companion object {
        private const val TAG: String = "ImageViewModel"
    }

    private var fetchImagesJob: Job? = null

    private var _currentPath = MutableStateFlow("")
    val currentPath = _currentPath.asStateFlow()

    private val pathStack = ArrayDeque<String>() // 用於追蹤路徑歷史
    val canGoBack: Boolean
        get() = pathStack.isNotEmpty()

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

    // 使用 MutableSharedFlow 讓view監聽該flow，用來傳遞刪除事件，
    private val _deletionEvent = MutableSharedFlow<DeletionEvent>()
    val deletionEvent = _deletionEvent.asSharedFlow()

    init {
        // 監聽 currentPath 的變化，調用 fetchImages 新操作
        viewModelScope.launch {
            currentPath.drop(1).collect { newPath ->
                Log.d(TAG, "currentPath changed to: $newPath")
                // 根據新的路徑讀取圖片
                fetchImages(newPath)
            }
        }
    }

    //若從Folder Image切回 All Image是否需要重新取?
    //重取會導致Composable刷新而返回top，不重取則無法取得最新資料。目前選擇：重取
    //取得path下所有image 含子資料夾
    fun fetchImages(path: String = "") {
        Log.d(TAG, "fetchImages() called with: path = $path")
        // 取消先前正在進行的 fetchImages 工作（如果有的話）
        fetchImagesJob?.cancel()

        // 啟動新的job
        fetchImagesJob = viewModelScope.launch {
//            Log.d(TAG,"fetchImages start")
            _imageList.value = emptyList()
            imageManager.getImageFlow(path)
                .catch { e ->
                    Log.e("ImageViewModel", "Error fetching images", e)
                }
                .collect { image ->
                    // 每讀到一筆資料就追加到現有列表中，避免圖片過多
                    _imageList.update { currentList ->
                        currentList + image
                    }
                }
//            Log.d(TAG,"fetchImages finish")
        }
    }

    fun deleteImage(image: ImageModel) {
        _selectedImages.value = setOf(image)
        deleteSelectedImages()
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            val selectedImagesList = _selectedImages.value.toList()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11 以上，一次請求多張圖刪除的確認 IntentSender
                val intentSender = imageManager.requestDeletionIntentSender(selectedImagesList)
                if (intentSender != null) {
                    _deletionEvent.emit(DeletionEvent.ShowDeletionConfirmation(selectedImagesList, intentSender))
                }
            } else {
                // Android 10 以下，逐一直接刪除
                selectedImagesList.forEach { image ->
                    val result = imageManager.deleteImageDirectly(image)
                    when (result) {
                        is DeleteResult.Success -> {
                            _deletionEvent.emit(DeletionEvent.DeletionResult(listOf(image), true))
                        }

                        is DeleteResult.Failure -> {
                            _deletionEvent.emit(DeletionEvent.DeletionResult(listOf(image), false))
                        }
                    }
                }
            }
            // 一旦發送刪除請求後清空選取圖片列表
            _selectedImages.value = emptySet()
        }
    }


    suspend fun fetchFolderPath() {
        //必須等待抓完當前資料夾含子資料夾圖片後
        fetchImagesJob?.join()
        _folderList.value = emptyList()
        val folderGrouping = folderRepository.getFirstLevel(_imageList.value)
        fetchCurrentFolderImages(folderGrouping.rootFolder)
        _folderList.value = folderGrouping.subFolders
        _currentPath.value = folderGrouping.rootFolder

    }

    suspend fun fetchCurrentFolderImages(currentFolder: String) {
        fetchImagesJob?.join()
        // 使用 trimEnd('/') 處理可能多餘的斜線
        val trimmedFolder = currentFolder.trimEnd('/')
        _folderImageList.value = _imageList.value.filter { image ->
            // 只有當 image.folderPath 與當前資料夾路徑完全一致時，才算直接屬於該資料夾
            image.folderPath.trimEnd('/') == trimmedFolder
        }
    }

    fun refreshCurrentFolderImages() {
        //避免不必要的計算
        if (_folderImageList.value.isEmpty()) return
        viewModelScope.launch {
            fetchImagesJob?.join()
            val trimmedFolder = currentPath.value.trimEnd('/')
            _folderImageList.value = _imageList.value.filter { image ->
                // 只有當 image.folderPath 與當前資料夾路徑完全一致時，才算直接屬於該資料夾
                image.folderPath.trimEnd('/') == trimmedFolder
            }
        }
    }

    fun backPath() {
        if (pathStack.isNotEmpty()) {
            val lastPart = pathStack.removeLast()
            _currentPath.value = _currentPath.value.removeSuffix(lastPart)
            viewModelScope.launch {
                fetchFolderPath()
            }
        }
    }

    fun nextPath(newPath: String) {
        _currentPath.value += newPath
        pathStack.addLast(newPath)
        // 啟動協程等待 fetchImagesJob 完成，再呼叫 fetchFolderPath
        viewModelScope.launch {
            fetchFolderPath()
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
            if (currentSet.contains(image)) currentSet - image
            else currentSet + image

        }
    }

    fun clearStack(){
        pathStack.clear()
    }
}