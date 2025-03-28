package com.example.imageapplication

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.imageapplication.component.DaggerAppComponent
import com.example.imageapplication.ui.theme.ImageApplicationTheme
import javax.inject.Inject
import android.Manifest
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.example.imageapplication.model.ImageModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.imageapplication.model.FolderModel

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageViewModel: ImageViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerAppComponent.factory().create(application).inject(this)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                showContent()
            } else {
                Toast.makeText(
                    this,
                    "Reading files permission is required to run this app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        enableEdgeToEdge()
        checkPermissionsAndProceed()

    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun showContent() {
        setContent {
            val isMultiSelectMode by imageViewModel.isMultiSelectMode.collectAsState()
            ImageApplicationTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }


                // 使用 LaunchedEffect，只在 selectedTabIndex 改變時調用對應的 fetch 方法
                LaunchedEffect(key1 = selectedTabIndex) {
                    when (selectedTabIndex) {
                        0 -> imageViewModel.fetchImages()      // 切換到 All Image 時 fetch 一次圖片
                        1 -> imageViewModel.fetchFolderPath()    // 切換到 Folder Image 時更新資料夾
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                if (isMultiSelectMode) {
                                    Text("多選模式")
                                } else {
                                    Text("Image Application")
                                }
                            },
                            actions = {
                                if (isMultiSelectMode) {
                                    // 垃圾桶按鈕：執行刪除操作
                                    IconButton(onClick = {
                                        // 在 ViewModel 中處理刪除選取的圖片
                                        imageViewModel.deleteSelectedImages()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "刪除"
                                        )
                                    }
                                    // 取消按鈕：退出多選模式
                                    IconButton(onClick = {
                                        imageViewModel.exitMultiSelectMode()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "取消"
                                        )
                                    }
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // TabRow：顯示兩個選項：All Image 與 Folder Image
                        val tabTitles = listOf("All Image", "Folder Image")
                        TabRow(selectedTabIndex = selectedTabIndex) {
                            tabTitles.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }

                        // 根據所選 Tab 顯示相應內容
                        Crossfade(targetState = selectedTabIndex, label = "") { tabIndex ->
                            when (tabIndex) {
                                0 -> AllImagesScreen(imageViewModel)
                                1 -> FolderImagesScreen(imageViewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AllImagesScreen(viewModel: ImageViewModel) {
        // 只有 All Image 這個會訂閱 imageList 狀態
        val imageList by viewModel.imageList.collectAsState()
        Log.d("AllImagesScreen", imageList.size.toString())
        MultiSelectImageScreen(viewModel = viewModel)
//        ImageGrid(imageList = imageList)
    }

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

    private fun checkPermissionsAndProceed() {
        val permissions: List<String> = when {
            // Android 13 (API 33) 及以上只需讀取媒體圖片權限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            // Android 10 (API 29) ~ Android 12 (API 32)：請求讀取與全部檔案存取權限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
                )
            }
            // Android 9 (API 28) 及以下：請求傳統的讀取與寫入權限
            else -> {
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }

        // 找出尚未授權的權限
        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // 如果所有權限已授予，則進入應用主流程
            showContent()
        } else {
            // 請求缺失的權限
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}


@Composable
fun ImageGrid(imageList: List<ImageModel>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(imageList) { image ->
            ImageItem(image = image)
        }
    }
}

@Composable
fun FolderImageGrid(
    viewModel: ImageViewModel,
    currentPath: String,
    imageList: List<ImageModel>,
    folderList: List<FolderModel>
) {
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
                item() {
                    FolderItem(onFolderClick = { viewModel.backPath() })
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
                    ImageItem(image = image)
                }
            }
        }
    }
}


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

//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun ImageItem(
//    image: ImageModel,
//    isSelected: Boolean = false,
//    onImageClick: (ImageModel) -> Unit,
//    onImageLongClick: (ImageModel) -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .padding(4.dp)
//            .fillMaxWidth()
//            .combinedClickable(
//                onClick = {
//                    // 單短擊，彈出 image 圖示或者執行其他操作
//                    onImageClick(image)
//                },
//                onLongClick = {
//                    // 長按，啟動多選功能
//                    onImageLongClick(image)
//                }
//            )
//    ) {
//        AsyncImage(
//            model = image.uri,
//            contentDescription = image.name,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(1f)
//                .clip(MaterialTheme.shapes.medium)
//        )
//        Spacer(modifier = Modifier.height(4.dp))
//        // 顯示圖片名稱，name 在下方
//        Text(
//            text = image.name,
//            style = MaterialTheme.typography.bodyMedium,
//            maxLines = 1,
//            modifier = Modifier.padding(horizontal = 4.dp)
//        )
//    }
//}
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
fun MultiSelectImageScreen(viewModel: ImageViewModel) {
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val imageList by viewModel.imageList.collectAsState()

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
                        // 單擊行為
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

