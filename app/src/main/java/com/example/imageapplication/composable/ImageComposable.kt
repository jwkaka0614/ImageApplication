package com.example.imageapplication.composable

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.imageapplication.ImageViewModel

@Composable
fun AllImagesScreen(viewModel: ImageViewModel) {
    // 只有 All Image 這個會訂閱 imageList 狀態
    val imageList by viewModel.imageList.collectAsState()
    Log.d("AllImagesScreen", imageList.size.toString())
    MultiSelectImageScreen(viewModel = viewModel)
//        ImageGrid(imageList = imageList)
}

@Composable
fun MultiSelectImageScreen(viewModel: ImageViewModel) {
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val imageList by viewModel.imageList.collectAsState()
    var previewIndex by remember { mutableIntStateOf(-1) }
    val showPreview = previewIndex != -1
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
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
