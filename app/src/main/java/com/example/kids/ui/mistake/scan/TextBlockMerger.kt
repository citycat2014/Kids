package com.example.kids.ui.mistake.scan

import android.graphics.Rect

/**
 * 题目块数据类
 * 表示合并后的完整题目区域
 */
data class QuestionBlock(
    val text: String,              // 合并后的完整文本
    val questionNumber: String?,   // 提取的题号
    val boundingBox: Rect,         // 合并后的边界框
    val sourceBlocks: List<Int>    // 包含的原始块索引
)

/**
 * 文本块合并器
 * 将 ML Kit 返回的零散文本块按题目边界智能合并
 */
object TextBlockMerger {

    // 默认阈值配置（当无法获取图片尺寸时使用）
    private const val DEFAULT_LINE_HEIGHT_THRESHOLD = 50
    private const val DEFAULT_HORIZONTAL_GAP_THRESHOLD = 150
    private const val MIN_QUESTION_LENGTH = 2  // 最小题目长度（降低要求以保留更多短题目）

    /**
     * 将零散的文本块智能合并为题目区域
     *
     * @param blocks OCR 识别的文本块列表
     * @param imageWidth 图片宽度（用于自适应阈值）
     * @param imageHeight 图片高度（用于自适应阈值）
     * @return 合并后的题目块列表
     */
    fun mergeIntoQuestions(
        blocks: List<TextBlock>,
        imageWidth: Int = 1440,
        imageHeight: Int = 1920
    ): List<QuestionBlock> {
        if (blocks.isEmpty()) return emptyList()

        // 根据图片尺寸计算自适应阈值
        val lineHeightThreshold = (imageHeight / 40).coerceAtLeast(20)
        val horizontalGapThreshold = (imageWidth / 10).coerceAtLeast(50)

        // 按垂直位置排序（从上到下）
        val sortedBlocks = blocks
            .mapIndexed { index, block -> IndexedBlock(index, block) }
            .sortedWith(compareBy({ it.block.boundingBox?.top ?: 0 }, { it.block.boundingBox?.left ?: 0 }))

        val questionGroups = mutableListOf<MutableList<IndexedBlock>>()
        var currentGroup = mutableListOf(sortedBlocks.first())

        // 遍历所有块，按规则分组
        for (i in 1 until sortedBlocks.size) {
            val currentBlock = sortedBlocks[i]
            val lastBlock = currentGroup.last()

            if (shouldMerge(lastBlock, currentBlock, lineHeightThreshold, horizontalGapThreshold)) {
                // 可以合并，加入当前组
                currentGroup.add(currentBlock)
            } else {
                // 不能合并，保存当前组，开始新组
                questionGroups.add(currentGroup)
                currentGroup = mutableListOf(currentBlock)
            }
        }

        // 添加最后一个组
        if (currentGroup.isNotEmpty()) {
            questionGroups.add(currentGroup)
        }

        // 将每组转换为 QuestionBlock
        return questionGroups.mapIndexed { groupIndex, group ->
            createQuestionBlock(group, groupIndex)
        }.filter { it.text.length >= MIN_QUESTION_LENGTH }
    }

    /**
     * 判断两个块是否应该合并为同一道题
     */
    private fun shouldMerge(
        block1: IndexedBlock,
        block2: IndexedBlock,
        lineHeightThreshold: Int,
        horizontalGapThreshold: Int
    ): Boolean {
        val box1 = block1.block.boundingBox ?: return false
        val box2 = block2.block.boundingBox ?: return false

        val text1 = block1.block.text.trim()
        val text2 = block2.block.text.trim()

        // 检查 block2 是否以新题号开头 - 如果是，绝对不能合并
        if (isQuestionStart(text2)) {
            return false
        }

        // 垂直方向检查
        val verticalDistance = box2.top - box1.bottom
        val lineHeight = estimateLineHeight(box1, box2, lineHeightThreshold)

        // 如果垂直距离过大，不能合并
        if (verticalDistance > lineHeightThreshold * 2) {
            return false
        }

        // 检查是否有明显的段落间距
        if (verticalDistance > lineHeight * 2.5) {
            return false
        }

        // 水平方向检查
        val horizontalGap = box2.left - box1.right

        // 同一行的块，水平间距不能太大
        if (isSameLine(block1, block2)) {
            if (horizontalGap > horizontalGapThreshold) {
                return false
            }
        }

        // 检查内容连贯性
        if (!isContentCoherent(text1, text2)) {
            return false
        }

        return true
    }

    /**
     * 检查文本是否以题号开头（表示新题目的开始）
     * 更全面的题号格式识别
     */
    private fun isQuestionStart(text: String): Boolean {
        val trimmed = text.trim()

        // 匹配常见的题号格式（按优先级排序）
        val patterns = listOf(
            // 带括号的中文数字：(一)、（1）
            "^[（\\(][一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾][）\\)][、.\\s]*",
            // 带括号的阿拉伯数字：(1)、（1）
            "^[（\\(]\\d{1,3}[）\\)][、.\\s]*",
            // 中文数字+标点：一、 二.
            "^[一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾]+[、.．\\s]+",
            // 阿拉伯数字+标点：1. 1、 1) 1】
            "^\\d{1,3}[、.．)\\]】][\\s]*",
            // 圆圈数字：①②③
            "^[①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳]",
            // 方括号：【1】
            "^【\\d+】",
            // 第X题：第1题、第一题
            "^第[一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾\\d]+题",
            // 题型标记：选择题、填空题等
            "^(选择题|填空题|判断题|解答题|计算题|应用题|作文题)[\\s:：]*"
        )

        return patterns.any { pattern ->
            Regex(pattern).containsMatchIn(trimmed)
        }
    }

    /**
     * 判断两个块是否在同一行
     */
    private fun isSameLine(block1: IndexedBlock, block2: IndexedBlock): Boolean {
        val box1 = block1.block.boundingBox ?: return false
        val box2 = block2.block.boundingBox ?: return false

        // 垂直位置重叠超过 40% 认为在同一行
        val overlapTop = maxOf(box1.top, box2.top)
        val overlapBottom = minOf(box1.bottom, box2.bottom)
        val overlapHeight = overlapBottom - overlapTop

        val height1 = box1.height()
        val height2 = box2.height()
        val minHeight = minOf(height1, height2)

        return overlapHeight > minHeight * 0.4
    }

    /**
     * 估算行高
     */
    private fun estimateLineHeight(box1: Rect, box2: Rect, defaultThreshold: Int): Int {
        return maxOf(box1.height(), box2.height(), defaultThreshold)
    }

    /**
     * 检查内容是否连贯
     * 修复逻辑：更准确地判断是否应该合并
     */
    private fun isContentCoherent(text1: String, text2: String): Boolean {
        val trimmed1 = text1.trim()
        val trimmed2 = text2.trim()

        // 情况1：text1 是选项（A/B/C/D），text2 也是选项 → 应该合并
        val isOption1 = isOptionFormat(trimmed1)
        val isOption2 = isOptionFormat(trimmed2)
        if (isOption1 && isOption2) {
            return true
        }

        // 情况2：text1 是题干，text2 是选项 → 应该合并
        val isQuestionLike1 = isQuestionLike(trimmed1)
        if (isQuestionLike1 && isOption2) {
            return true
        }

        // 情况3：text1 以标点结尾，text2 不是题号 → 可能是同一题的不同行
        // 但需要检查 text1 是否是一个完整的句子（有句号等）
        val sentenceEndMarks = listOf("。", "？", "?", "！", "!", "；", ";"
        )
        val text1EndsWithSentenceEnd = sentenceEndMarks.any { trimmed1.endsWith(it) }

        // 情况4：如果 text1 以标点结尾，且 text2 看起来是完整的新内容（不是选项）
        // 且 text1 本身就很长（可能是一个完整题目），则不应该合并
        if (text1EndsWithSentenceEnd && trimmed1.length > 20 && !isOption2) {
            // 如果 text2 也有题号特征，说明是新题
            if (isQuestionLike(trimmed2)) {
                return false
            }
        }

        // 情况5：text2 包含明显的分隔词
        val separatorWords = listOf(
            "解析", "答案", "解答", "解", "证明", "计算过程", "步骤"
        )
        if (separatorWords.any { trimmed2.startsWith(it) }) {
            // 可能是新段落的开始
            return false
        }

        // 默认情况：允许合并（让空间规则起主导作用）
        return true
    }

    /**
     * 判断是否是选项格式（A/B/C/D）
     */
    private fun isOptionFormat(text: String): Boolean {
        return text.matches(Regex("^[A-Da-dＡ-Ｄａ-ｄ][.．、\\s:：)\\)][\\s]*.*")) ||
               text.matches(Regex("^[A-Da-dＡ-Ｄａ-ｄ][\\s]*.*"))
    }

    /**
     * 判断是否是题目（包含题号或问句）
     */
    private fun isQuestionLike(text: String): Boolean {
        // 包含问号
        if (text.contains("?") || text.contains("？")) return true
        // 包含题号
        if (isQuestionStart(text)) return true
        // 包含常见题目关键词
        val keywords = listOf("选择", "填空", "解答", "计算", "证明", "求解", "判断")
        return keywords.any { text.contains(it) }
    }

    /**
     * 从文本中提取题号
     */
    private fun extractQuestionNumber(text: String): String? {
        val trimmed = text.trim()

        val patterns = listOf(
            Regex("^[（\\(](\\d+)[）\\)][、.\\s]*"),     // （1）(1)
            Regex("^(\\d+)[、.．)\\]】][\\s]*"),              // 1. 1、
            Regex("^([①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳])"),     // ①②③
            Regex("^([一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾]+)[、.\\s]*"), // 一、二、
            Regex("^【(\\d+)】"),                       // 【1】
            Regex("^第([一二三四五六七八九十壹贰叁肆伍陆柒捌玖拾\\d]+)题")   // 第一题、第1题
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null) {
                return match.groupValues[1].ifEmpty { match.groupValues[0] }
            }
        }

        return null
    }

    /**
     * 将一组文本块转换为 QuestionBlock
     */
    private fun createQuestionBlock(
        group: List<IndexedBlock>,
        groupIndex: Int
    ): QuestionBlock {
        // 按原始顺序排序（保持题内文本顺序）
        val sortedGroup = group.sortedWith(
            compareBy({ it.block.boundingBox?.top ?: 0 }, { it.block.boundingBox?.left ?: 0 })
        )

        // 合并文本（按行合并）
        val fullText = sortedGroup.joinToString("\n") { it.block.text.trim() }

        // 提取题号（从第一个块）
        val questionNumber = extractQuestionNumber(sortedGroup.first().block.text)

        // 计算合并后的边界框
        val boxes = sortedGroup.mapNotNull { it.block.boundingBox }
        val mergedBox = mergeBoundingBoxes(boxes)

        // 获取原始块索引
        val sourceIndices = sortedGroup.map { it.index }

        return QuestionBlock(
            text = fullText,
            questionNumber = questionNumber,
            boundingBox = mergedBox,
            sourceBlocks = sourceIndices
        )
    }

    /**
     * 合并多个边界框
     */
    private fun mergeBoundingBoxes(boxes: List<Rect>): Rect {
        if (boxes.isEmpty()) return Rect(0, 0, 0, 0)
        if (boxes.size == 1) return Rect(boxes[0])

        var left = Int.MAX_VALUE
        var top = Int.MAX_VALUE
        var right = Int.MIN_VALUE
        var bottom = Int.MIN_VALUE

        for (box in boxes) {
            left = minOf(left, box.left)
            top = minOf(top, box.top)
            right = maxOf(right, box.right)
            bottom = maxOf(bottom, box.bottom)
        }

        return Rect(left, top, right, bottom)
    }

    /**
     * 带索引的文本块（用于跟踪原始位置）
     */
    private data class IndexedBlock(
        val index: Int,
        val block: TextBlock
    )
}
