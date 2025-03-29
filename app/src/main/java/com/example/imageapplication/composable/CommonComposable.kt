package com.example.imageapplication.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.imageapplication.model.FolderModel
import com.example.imageapplication.model.ImageModel

@Composable
fun FolderItem(
    folder: FolderModel = FolderModel("", "..."),
    onFolderClick: (folderPath: String) -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Gray, shape = RoundedCornerShape(8.dp))
                    .clickable { onFolderClick(folder.folderPath) }
                    .padding(10.dp)
            ) {
                Text(
                    text = folder.folderName,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageItem(
    image: ImageModel,
    onImageClick: (ImageModel) -> Unit = {},
    onImageLongClick: (ImageModel) -> Unit = {},
    isSelected: Boolean = false
) {
    // 使用 Box 包裝，這樣可以在圖片上覆蓋其他組件顯示選取狀態
    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onImageClick(image) },
                onLongClick = { onImageLongClick(image) }
            )
    ) {
        // 圖片及說明的內容
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AsyncImage(
                model = image.uri,
                contentDescription = image.name,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = image.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        // 如果處於選取狀態，覆蓋一個半透明的遮罩和勾選圖示
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x66000000)) // 半透明黑色遮罩
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageList: List<ImageModel>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onDeleteImage: (ImageModel) -> Unit,
    onNavigate: (Int) -> Unit
) {
    // 使用 Dialog 呈現全螢幕的圖片預覽
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .clip(MaterialTheme.shapes.medium)
                .pointerInput(currentIndex) {
                    // 使用變數累積拖動距離
                    var dragOffset = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset += dragAmount
                        },
                        onDragEnd = {
                            val threshold = 100f
                            when {
                                dragOffset > threshold && currentIndex > 0 ->
                                    onNavigate(currentIndex - 1)

                                dragOffset < -threshold && currentIndex < imageList.size - 1 ->
                                    onNavigate(currentIndex + 1)
                            }
                            dragOffset = 0f
                        }
                    )
                },
        ) {
            // 當前圖片
            val image = imageList[currentIndex]
            AsyncImage(
                model = image.uri,
                contentDescription = image.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
            // 左側導覽按鈕，當有上一張時顯示
            if (currentIndex > 0) {
                IconButton(
                    onClick = { onNavigate(currentIndex - 1) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "上一張",
                        tint = Color.White
                    )
                }
            }
            // 右側導覽按鈕，當有下一張時顯示
            if (currentIndex < imageList.size - 1) {
                IconButton(
                    onClick = { onNavigate(currentIndex + 1) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下一張",
                        tint = Color.White
                    )
                }
            }
            // 底部刪除按鈕
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x88000000))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDeleteImage(image) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

