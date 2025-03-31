package com.example.imageapplication

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import com.example.imageapplication.component.DaggerTestAppComponent
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

    }
    @Test
    fun launchAppManually() {
        // 啟動你的主畫面
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            // 此時 imageViewModel 等依賴是由 TestAppComponent 注入 (如 TestImageManager)
            // 你可以檢查依賴是否為預期的測試版本或進行手動操作驗證
        }
        Thread.sleep(Long.MAX_VALUE)
    }
    @Test
    fun testGetImageFlow() {
        // 測試 TestImageManager 的 getImageFlow，進行對應驗證
    }
    // 您可以針對生產環境 MainActivity 的其他功能編寫更多測試
}