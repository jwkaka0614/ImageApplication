package com.example.imageapplication.manager

import android.content.Context
import com.example.imageapplication.model.FoldModel
import com.example.imageapplication.model.FolderGrouping
import com.example.imageapplication.model.ImageModel

class FolderRepository(private val context: Context) {
    companion object {
        private const val TAG: String = "FolderRepository"

    }
    /**1.取共同根目錄
    2.取共同根目錄下之子目錄及圖片
    子目錄以集合顯示**/

    /**
     * 根據所有圖片數據，先取得全局根目錄，再根據全局根目錄下的第一層子目錄分組，
     * 並對每個分組計算共同路徑。
     */
    fun getFirstLevel(imageList: List<ImageModel>): FolderGrouping {
        // 1. 取得共同根目錄（global root）
        val globalRoot = getCommonRoot(imageList.map { it.folderPath })

        // 2. 根據 globalRoot 對圖片分組，鍵為第一個資料夾名稱
        val groups = groupImagesByFirstLevel(imageList, globalRoot)

        // 3. 對於每一組，計算該組的共同路徑
        val foldList = groups.map { (firstLevel, images) ->
            // 這裡，用組內所有 folderPath 計算共同前綴
            val groupCommon = getCommonRoot(images.map { it.folderPath })
            val lastFolderName = groupCommon.trimEnd('/').split("/").lastOrNull() ?: ""
            FoldModel(folderName = lastFolderName, folderPath = groupCommon)
        }
        return FolderGrouping(rootFolder = globalRoot, subFolders = foldList)
    }

    /**
     * 取得多個路徑的共同根目錄
     */
    fun getCommonRoot(paths: List<String>): String {
        if (paths.isEmpty()) return ""
        val splitPaths = paths.map { it.trim('/').split("/") }
        val commonSegments = mutableListOf<String>()
        val minSize = splitPaths.minOf { it.size }
        for (i in 0 until minSize) {
            val segment = splitPaths.first()[i]
            if (splitPaths.all { it[i] == segment }) {
                commonSegments.add(segment)
            } else break
        }
        return if (commonSegments.isNotEmpty()) "/" + commonSegments.joinToString("/") + "/" else ""
    }

    /**
     * 根據 globalRoot 分組圖片，分組鍵為去除 globalRoot 後的第一個路徑片段。
     */
    fun groupImagesByFirstLevel(
        imageList: List<ImageModel>,
        globalRoot: String
    ): Map<String, List<ImageModel>> {
        val normalizedRoot = globalRoot.trimEnd('/') + "/"
        return imageList.filter { it.folderPath.startsWith(normalizedRoot) }
            .groupBy { image ->
                val relativePath = image.folderPath.removePrefix(normalizedRoot)
                // 取第一個片段作為分組鍵
                relativePath.split("/").firstOrNull() ?: ""
            }
    }

}