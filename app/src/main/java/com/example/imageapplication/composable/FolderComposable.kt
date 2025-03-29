package com.example.imageapplication.composable

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.imageapplication.ImageViewModel
import com.example.imageapplication.model.FolderModel
import com.example.imageapplication.model.ImageModel

@Composable
fun FolderImagesScreen(viewModel: ImageViewModel) {
    // 只有 Folder Image 會訂閱 folderImageList、folderList、currentPath
    val folderImages by viewModel.folderImageList.collectAsState()
    val folderList by viewModel.folderList.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    Log.d("FolderImagesScreen", "currentPath: $currentPath")
    FolderImageGrid(
        viewModel = viewModel,
        currentPath = currentPath,
        imageList = folderImages,
        folderList = folderList
    )
}

@Composable
fun FolderImageGrid(
    viewModel: ImageViewModel,
    currentPath: String,
    imageList: List<ImageModel>,
    folderList: List<FolderModel>
) {
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    var previewIndex by remember { mutableIntStateOf(-1) }
    val showPreview = previewIndex != -1
    Column {
        Text(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 8.dp),
            text = currentPath,
            style = MaterialTheme.typography.bodyMedium
        )

        BoxWithConstraints {
            val minSize = 150.dp
            //計算列數，用於 GridItemSpan
            val columns = (maxWidth / minSize).toInt().coerceAtLeast(1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                if (viewModel.canGoBack) {
                    item {
                        FolderItem(onFolderClick = { viewModel.backPath() })
                    }
                }
                //資料夾區
                items(folderList) { folder ->
                    FolderItem(
                        folder = folder,
                        onFolderClick = { folderPath ->
                            viewModel.nextPath(folderPath)
                        })
                }
                //資料夾與圖片分區
                item(span = { GridItemSpan(columns) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                //圖片區
                items(imageList) { image ->
                    ImageItem(
                        image = image,
                        isSelected = selectedImages.contains(image),
                        onImageClick = {
                            if (isMultiSelectMode) {
                                viewModel.toggleImageSelection(image)
                            } else {
                                previewIndex = imageList.indexOf(image)
                            }
                        },
                        onImageLongClick = {
                            viewModel.enterMultiSelectMode()
                            viewModel.toggleImageSelection(image)
                        }
                    )
                }
            }
        }
    }
    // 當預覽狀態為開啟時，顯示圖片預覽 Dialog
    if (showPreview) {
        ImagePreviewDialog(
            imageList = imageList,
            currentIndex = previewIndex,
            onDismiss = { previewIndex = -1 },
            onDeleteImage = { image ->
                viewModel.deleteImage(image)
                previewIndex = -1
            },
            onNavigate = { newIndex ->
                previewIndex = newIndex
            }
        )
    }
}
