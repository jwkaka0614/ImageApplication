package com.example.imageapplication.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.imageapplication.model.ImageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import android.net.Uri
import com.example.imageapplication.model.DeleteResult
import kotlinx.coroutines.delay
import kotlin.random.Random

@Singleton
class TestImageManager @Inject constructor(private val context: Context) : IImageManager {
    /**
     * 若為 true，則 requestDeletionIntentSender 會回傳一個非 null 的 IntentSender。
     * 測試時可以根據需要調整此旗標以模擬 Android 11 及以上行為。
     */
    var shouldReturnIntentSender: Boolean = true

    /**
     * 如有需要，可事先設定一個假 IntentSender，由此方法直接回傳。
     */
    var fakeIntentSender: IntentSender? = null

    /**
     * 刪除操作直接回傳的結果，預設為 true 表示成功。
     */
    var deleteSuccess: Boolean = true

    override fun getImageFlow(rootPath: String): Flow<ImageModel> = flow {
        // 測試我們先定義一組可能的資料夾名稱
        val folderNames = listOf("Nature", "Travel", "Food", "Animals", "Urban", "Sports", "Abstract")

        // 模擬生成 20 筆測試圖片資料
        val testImages = mutableListOf<ImageModel>()
        repeat(20) { index ->
            // 隨機決定目錄層數，介於 1 到 3 層
            val layerCount = Random.nextInt(1, 4)
            // 從 folderNames 當中隨機挑選幾個資料夾名稱當作各層資料夾
            val selectedFolders = (1..layerCount).map { folderNames.random() }
            // 組合成完整的資料夾路徑，如 "/Nature/Travel/Food/"
            val folderPath = "/" + selectedFolders.joinToString("/") + "/"
            // 圖片名稱，這裡以 "Image_{index}.jpg" 為範例
            val imageName = "Image_${index + 1}.jpg"
            // 模擬圖片的 Uri，例如 "content://test/Nature/Travel/Food/Image_1.jpg"
            val testUri = Uri.parse("content://test$folderPath$imageName")

            // 將該筆測試數據加入清單
            testImages.add(ImageModel(uri = testUri, name = imageName, folderPath = folderPath))
        }

        // 模擬延遲（例如網路或磁碟 I/O），並逐筆發射
        for (image in testImages) {
            emit(image)
            delay(50) // 每發射一筆資料後等待 50 毫秒，模擬 I/O 延遲
        }
    }

    /**
     * 根據設定決定是否回傳一個假 IntentSender：
     * - 若 [shouldReturnIntentSender] 為 false，則回傳 null；
     * - 否則，若 [fakeIntentSender] 非 null 則直接回傳，
     *   不然利用 [createDummyIntentSender] 產生一個 dummy IntentSender.
     */
    override fun requestDeletionIntentSender(images: List<ImageModel>): IntentSender? {
        return if (!shouldReturnIntentSender) {
            null
        } else {
            fakeIntentSender ?: createDummyIntentSender()
        }
    }
    /**
     * 模擬直接刪除圖片，回傳結果依據 [deleteSuccess] 的值。
     */
    override suspend fun deleteImageDirectly(image: ImageModel): DeleteResult  {
        return DeleteResult.Success(image)
    }

    /**
     * 為測試建立一個 dummy 的 IntentSender。
     *
     * 此處利用 PendingIntent 產生一個 IntentSender，
     * 注意：在單元測試環境下，需使用具備 Context 的測試工具（例如 Robolectric）或傳入測試用的 Dummy Context。
     */
    private fun createDummyIntentSender(): IntentSender {
        val dummyIntent = Intent(context, javaClass)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            dummyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pendingIntent.intentSender
    }
}