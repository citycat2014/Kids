package com.example.kids.ui.mistake.scan.paddle

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * OCR 预测器
 * 封装 OCRPredictorNative，提供简化的 OCR 接口
 */
class Predictor {

    companion object {
        private const val TAG = "Predictor"
        private const val CPU_THREAD_NUM = 4
        private const val CPU_POWER_MODE = "LITE_POWER_HIGH"
        private const val MAX_SIDE_LEN = 1280  // 增加以保留更多文字细节
        private const val MIN_CONFIDENCE = 0.4f  // 降低以保留更多识别结果
    }

    private var isInitialized = false
    private var nativePredictor: OCRPredictorNative? = null
    private var labels: List<String> = emptyList()

    /**
     * 初始化预测器
     */
    suspend fun init(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) {
            return@withContext true
        }

        try {
            // 确保模型文件已准备好
            if (!PaddleOcrModelManager.isModelsReady(context)) {
                Log.d(TAG, "Copying models from assets...")
                PaddleOcrModelManager.copyModelsFromAssets(context)
            }

            // 获取模型路径
            val modelDir = PaddleOcrModelManager.getModelDirectory(context)
            val labelFile = PaddleOcrModelManager.getLabelFile(context)

            val detModelPath = File(modelDir, "det_db.nb").absolutePath
            val recModelPath = File(modelDir, "rec_crnn.nb").absolutePath
            val clsModelPath = File(modelDir, "cls.nb").absolutePath

            Log.d(TAG, "Model paths:")
            Log.d(TAG, "  det: $detModelPath")
            Log.d(TAG, "  rec: $recModelPath")
            Log.d(TAG, "  cls: $clsModelPath")

            // 加载标签
            labels = loadLabels(labelFile.absolutePath)
            Log.d(TAG, "Loaded ${labels.size} labels")

            // 创建配置
            val config = OCRPredictorNative.Config().apply {
                useOpencl = 0
                cpuThreadNum = CPU_THREAD_NUM
                cpuPowerMode = CPU_POWER_MODE
                detModelFilename = detModelPath
                recModelFilename = recModelPath
                clsModelFilename = clsModelPath
            }

            // 初始化 native 预测器
            nativePredictor = OCRPredictorNative(config)

            isInitialized = nativePredictor?.isInitialized() ?: false
            Log.d(TAG, "Predictor initialization result: $isInitialized")
            isInitialized

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize predictor: ${e.message}", e)
            isInitialized = false
            false
        }
    }

    /**
     * 运行 OCR 识别
     */
    suspend fun runOcr(bitmap: Bitmap): List<OcrResultModel> = withContext(Dispatchers.Default) {
        if (!isInitialized || nativePredictor == null) {
            Log.e(TAG, "Predictor not initialized")
            return@withContext emptyList()
        }

        try {
            // 运行完整的 OCR 流程：检测 + 分类 + 识别
            val results = nativePredictor!!.runImage(
                bitmap,
                MAX_SIDE_LEN,
                1, // run_det
                1, // run_cls
                1  // run_rec
            )

            Log.d(TAG, "OCR completed, found ${results.size} text regions")

            // 后处理：将 word index 转换为文字
            for (result in results) {
                val word = StringBuilder()
                for (index in result.wordIndex) {
                    if (index >= 0 && index < labels.size) {
                        word.append(labels[index])
                    } else {
                        Log.e(TAG, "Word index out of range: $index (labels size: ${labels.size})")
                        word.append("×")
                    }
                }
                result.label = word.toString()
                result.clsLabel = if (result.clsIdx == 1) "180" else "0"

                Log.d(TAG, "Result: ${result.label}, confidence: ${result.confidence}")
            }

            // 过滤低置信度结果
            results.filter { it.confidence >= MIN_CONFIDENCE }

        } catch (e: Exception) {
            Log.e(TAG, "OCR failed: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 加载标签文件
     * 格式与官方 demo 一致
     */
    private fun loadLabels(path: String): List<String> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.e(TAG, "Label file not found: $path")
                return emptyList()
            }

            val lines = file.readLines().filter { it.isNotEmpty() }
            // 插入 "black" 在索引 0，与官方 demo 一致
            val result = mutableListOf("black")
            result.addAll(lines)
            result.add(" ")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load labels: ${e.message}")
            emptyList()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        nativePredictor?.destroy()
        nativePredictor = null
        isInitialized = false
        Log.d(TAG, "Predictor released")
    }

    /**
     * 检查是否已初始化
     */
    fun isReady(): Boolean {
        return isInitialized && nativePredictor?.isInitialized() == true
    }
}