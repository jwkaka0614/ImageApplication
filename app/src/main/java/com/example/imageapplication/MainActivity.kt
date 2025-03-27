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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.imageapplication.component.DaggerAppComponent
import com.example.imageapplication.ui.theme.ImageApplicationTheme
import javax.inject.Inject
import android.Manifest
import com.example.imageapplication.model.ImageModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageViewModel: ImageViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerAppComponent.factory().create(application).inject(this)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
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
        checkPermissionAndProceed()

    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun showContent() {
        setContent {
            ImageApplicationTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val imageList by imageViewModel.imageList.collectAsState()
                imageViewModel.fetchImages()
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Image Application") }
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

                        // 根據所選 Tab，顯示相應內容
                        when (selectedTabIndex) {
                            0 -> ImageGrid(imageList = imageList)
                            1 -> FolderImageGrid(imageList = imageList)
                        }
                    }
                }
            }
        }
    }


    private fun checkPermissionAndProceed() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 以上使用 READ_MEDIA_IMAGES
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            // 其他版本使用 READ_EXTERNAL_STORAGE
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // 權限已授予，直接顯示內容
            showContent()
        } else {
            // 沒有權限，請求權限
            requestPermissionLauncher.launch(permission)
        }


    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageApplicationTheme {
        Greeting("Android")
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = image.uri,
                    contentDescription = image.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 顯示圖片名稱，name 在下方
                Text(
                    text = image.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
// 顯示資料夾分組後的圖片 Grid
@Composable
fun FolderImageGrid(imageList: List<ImageModel>) {
    // 根據 folderPath 分組
    val groupedImages = imageList.groupBy { it.folderPath }
    Column(modifier = Modifier.fillMaxSize()) {
        groupedImages.forEach { (folder, images) ->
            // Folder header
            Text(
                text = folder.ifEmpty { "Unknown Folder" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                // 內邊距可以根據情況調整
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
            ) {
                items(images) { image ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = image.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = image.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}