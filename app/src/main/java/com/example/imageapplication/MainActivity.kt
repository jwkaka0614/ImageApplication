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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.imageapplication.component.DaggerAppComponent
import com.example.imageapplication.ui.theme.ImageApplicationTheme
import javax.inject.Inject
import android.Manifest
import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.imageapplication.component.AppComponent
import com.example.imageapplication.composable.AllImagesScreen
import com.example.imageapplication.composable.FolderImagesScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), AppInjectionProvider {
    lateinit var appComponent: AppComponent

    @Inject
    lateinit var imageViewModel: ImageViewModel
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    override fun inject(activity: MainActivity) {
        appComponent.inject(activity)
    }

    private val deletionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "圖片已成功刪除", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "使用者取消了刪除", Toast.LENGTH_SHORT).show()
        }
        imageViewModel.fetchImages(imageViewModel.currentPath.value)
        imageViewModel.refreshCurrentFolderImages()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // MainActivity.kt
        if ((application as? AppInjectionProvider) != null) {
            // 既然Application實作了該接口，就通過接口注入
            (application as AppInjectionProvider).inject(this)
        } else {
            // 如果不符合，就使用生產預設注入方式
            DaggerAppComponent.factory().create(application).inject(this)
        }
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
        lifecycleScope.launch {
            // 當生命週期至少處於 STARTED 狀態時開始收集
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                imageViewModel.deletionEvent.collect { event ->
                    when (event) {
                        is DeletionEvent.ShowDeletionConfirmation -> {
                            // 建立 IntentSenderRequest 並啟動刪除確認流程
                            val intentSenderRequest =
                                IntentSenderRequest.Builder(event.intentSender).build()
                            deletionLauncher.launch(intentSenderRequest)
                        }

                        is DeletionEvent.DeletionResult -> {
                            if (event.success) {
                                Toast.makeText(this@MainActivity, "圖片刪除成功", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "圖片刪除失敗", Toast.LENGTH_SHORT).show()
                            }
                            imageViewModel.fetchImages(imageViewModel.currentPath.value)
                            imageViewModel.refreshCurrentFolderImages()
                        }
                    }
                }
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
                                    IconButton(
                                        onClick = { imageViewModel.deleteSelectedImages() }
                                    ) {
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












