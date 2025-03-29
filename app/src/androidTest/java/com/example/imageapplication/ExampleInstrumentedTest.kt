package com.example.imageapplication

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import com.example.imageapplication.manager.IImageManager
import com.example.imageapplication.manager.TestImageManager

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import javax.inject.Inject

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {


    private lateinit var application: TestApplication
    private lateinit var mainActivity: MainActivity

    @Inject
    lateinit var imageManager: IImageManager // 這將會是 TestImageManager

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext() as TestApplication
        application.onCreate() // 初始化 TestApplication 及其元件

        // 手動創建 MainActivity (用於單元測試，並非完整的 Activity 啟動)
        mainActivity = MainActivity()
        application.inject(mainActivity) // 將依賴注入到 MainActivity 中
    }


    // 您可以針對生產環境 MainActivity 的其他功能編寫更多測試
}