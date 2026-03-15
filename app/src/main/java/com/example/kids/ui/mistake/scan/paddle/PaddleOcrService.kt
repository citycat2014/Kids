package com.example.kids.ui.mistake.scan.paddle

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.example.kids.ui.mistake.scan.OcrResult
import com.example.kids.ui.mistake.scan.TextBlock
import com.example.kids.ui.mistake.scan.TextElement
import com.example.kids.ui.mistake.scan.TextLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * PaddleOCR 服务
 * 封装 PaddleOCR 文字识别功能，与现有 OcrService 接口兼容
 *
 * 注意：使用 PaddleOCR 需要预装 PaddleLite 库文件和模型文件：
 * - app/PaddleLite/cxx/libs/${ANDROID_ABI}/libpaddle_light_api_shared.so
 * - app/src/main/assets/models/det_db.nb (检测模型)
 * - app/src/main/assets/models/rec_crnn.nb (识别模型)
 * - app/src/main/assets/models/cls.nb (分类模型)
 * - app/src/main/assets/labels/ppocr_keys_v1.txt (字典文件)
 */
class PaddleOcrService(private val context: Context) {

    companion object {
        private const val TAG = "PaddleOcrService"
        private const val MIN_CONFIDENCE = 0.4f  // 降低以保留更多识别结果
    }

    private val predictor = Predictor()
    private var isInitialized = false

    /**
     * 初始化 OCR 服务
     * 在使用 recognizeText 之前需要确保初始化成功
     */
    suspend fun initialize(): Boolean {
        if (isInitialized) {
            return true
        }

        isInitialized = predictor.init(context)
        Log.d(TAG, "PaddleOCR initialization result: $isInitialized")
        return isInitialized
    }

    /**
     * 识别图片中的文字
     *
     * @param imageUri 图片 URI
     * @return OCR 识别结果
     */
    suspend fun recognizeText(imageUri: Uri): OcrResult {
        // 确保已初始化
        if (!isInitialized) {
            val initSuccess = initialize()
            if (!initSuccess) {
                Log.w(TAG, "Failed to initialize PaddleOCR, falling back to empty result")
                // 如果初始化失败，返回空结果而不是错误
                // 这样可以保证应用在库文件未就绪时也能正常运行
                return OcrResult.Success(emptyList(), "", 0, 0)
            }
        }

        return try {
            // 加载图片
            val bitmap = loadBitmap(imageUri)
                ?: return OcrResult.Error("无法加载图片")

            // 记录 bitmap 尺寸
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height
            Log.d(TAG, "OCR bitmap size: ${bitmapWidth}x${bitmapHeight}")

            // 运行 OCR
            val ocrResults = runOcr(bitmap)

            // 转换为 TextBlock 格式
            val textBlocks = convertToTextBlocks(ocrResults)

            // 构建完整文本
            val fullText = buildFullText(textBlocks)

            OcrResult.Success(textBlocks, fullText, bitmapWidth, bitmapHeight)

        } catch (e: Exception) {
            Log.e(TAG, "OCR recognition failed: ${e.message}", e)
            OcrResult.Error(e.message ?: "OCR识别失败")
        }
    }

    /**
     * 运行 OCR 识别
     */
    private suspend fun runOcr(bitmap: Bitmap): List<OcrResultModel> {
        return predictor.runOcr(bitmap)
    }

    /**
     * 加载 Bitmap 并处理 EXIF 旋转信息
     * 解决 OCR 识别框偏移问题：手机拍摄的照片包含 EXIF 旋转信息，
     * BitmapFactory.decodeStream 不会自动处理，但 Coil 的 AsyncImage 会，
     * 导致坐标系不一致，框选位置偏移。
     */
    private suspend fun loadBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. 加载原始 Bitmap
            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                android.graphics.BitmapFactory.decodeStream(input)
            } ?: return@withContext null

            // 2. 读取 EXIF 旋转信息
            val orientation = getExifOrientation(uri)

            // 3. 根据旋转信息调整 Bitmap
            rotateBitmapIfNeeded(bitmap, orientation)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bitmap: ${e.message}")
            null
        }
    }

    /**
     * 获取图片的 EXIF 旋转方向
     */
    private fun getExifOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read EXIF orientation: ${e.message}")
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * 根据 EXIF 旋转信息旋转 Bitmap
     */
    private fun rotateBitmapIfNeeded(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                matrix.setScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setScale(1f, -1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap // ORIENTATION_NORMAL 或其他无需处理
        }

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    /**
     * 将 PaddleOCR 结果转换为 TextBlock 格式
     * 保持与现有接口兼容
     */
    private fun convertToTextBlocks(ocrResults: List<OcrResultModel>): List<TextBlock> {
        return ocrResults
            .filter { it.confidence >= MIN_CONFIDENCE }
            .sortedBy { it.toBoundingRect().top }
            .map { result ->
                convertToTextBlock(result)
            }
    }

    /**
     * 将单个 OcrResultModel 转换为 TextBlock
     */
    private fun convertToTextBlock(result: OcrResultModel): TextBlock {
        val rect = result.toBoundingRect()

        // 创建 TextElement（PaddleOCR 没有细粒度元素，所以整个文本作为一个元素）
        val elements = listOf(
            TextElement(
                text = result.label,
                boundingBox = rect
            )
        )

        // 创建 TextLine
        val lines = listOf(
            TextLine(
                text = result.label,
                boundingBox = rect,
                elements = elements
            )
        )

        return TextBlock(
            text = result.label,
            boundingBox = rect,
            lines = lines
        )
    }

    /**
     * 从 TextBlock 列表构建完整文本
     */
    private fun buildFullText(blocks: List<TextBlock>): String {
        return blocks.joinToString("\n") { it.text }
    }

    /**
     * 尝试从文字中提取题号
     * 支持的格式：1. 、2、 、(1) 、（1）等
     */
    fun extractQuestionNumber(text: String): String? {
        // 匹配常见的题号格式
        val patterns = listOf(
            "^[（\\(]?(\\d+)[）\\)]?[、.\\s]",  // (1)、（1）、1.、1、
            "^[一二三四五六七八九十]+[、.\\s]",     // 一、二、
            "^[①②③④⑤⑥⑦⑧⑨⑩]"                    // ①②③
        )

        for (pattern in patterns) {
            val regex = Regex(pattern)
            val match = regex.find(text.trim())
            if (match != null) {
                return match.value.trim()
            }
        }
        return null
    }

    /**
     * 判断文字是否可能是题目
     * 基于启发式规则：包含问号、有题号格式、长度适中等
     */
    fun isLikelyQuestion(text: String): Boolean {
        val trimmed = text.trim()

        // 包含问号的通常是题目
        if (trimmed.contains("?") || trimmed.contains("？")) return true

        // 有题号格式
        if (extractQuestionNumber(trimmed) != null) return true

        // 包含"选择"、"填空"、"解答"等关键词
        val keywords = listOf("选择", "填空", "解答", "计算", "证明", "求解", "求")
        if (keywords.any { trimmed.contains(it) }) return true

        return false
    }

    /**
     * 释放资源
     */
    fun release() {
        predictor.release()
        isInitialized = false
        Log.d(TAG, "PaddleOcrService released")
    }

    /**
     * 检查服务是否已准备好
     */
    fun isReady(): Boolean {
        return isInitialized && predictor.isReady()
    }
}

/**
 * OCR 服务工厂
 * 用于创建合适的服务实例
 */
object OcrServiceFactory {

    /**
     * 创建默认 OCR 服务
     * 目前返回 PaddleOcrService（如果可用）
     *
     * @param context 上下文
     * @return OcrService 实例
     */
    fun create(context: Context): Any {
        return PaddleOcrService(context)
    }
}
