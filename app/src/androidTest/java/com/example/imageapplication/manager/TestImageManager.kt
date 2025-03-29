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
        val trimmedRoot = rootPath.trimEnd('/')
        // 模擬第一筆資料
        val testImage1 = ImageModel(
            uri = Uri.parse("content://test/image/1"),
            name = "TestImage1",
            folderPath = if (trimmedRoot.isEmpty()) "/test" else trimmedRoot
        )
        // 模擬第二筆資料
        val testImage2 = ImageModel(
            uri = Uri.parse("content://test/image/2"),
            name = "TestImage2",
            folderPath = if (trimmedRoot.isEmpty()) "/test" else trimmedRoot
        )
        emit(testImage1)
        emit(testImage2)
    }

    /**
     * 根據設定決定是否回傳一個假 IntentSender：
     * - 若 [shouldReturnIntentSender] 為 false，則回傳 null；
     * - 否則，若 [fakeIntentSender] 非 null 則直接回傳，
     *   不然利用 [createDummyIntentSender] 產生一個 dummy IntentSender.
     */
    override fun requestDeletionIntentSender(image: ImageModel): IntentSender? {
        return if (!shouldReturnIntentSender) {
            null
        } else {
            fakeIntentSender ?: createDummyIntentSender()
        }
    }
    /**
     * 模擬直接刪除圖片，回傳結果依據 [deleteSuccess] 的值。
     */
    override fun deleteImageDirectly(image: ImageModel): Boolean {
        return deleteSuccess
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