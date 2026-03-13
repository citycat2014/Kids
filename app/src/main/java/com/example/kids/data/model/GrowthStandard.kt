package com.example.kids.data.model

/**
 * 儿童成长标准数据
 * 基于2025年儿童身高体重标准表
 */
object GrowthStandard {

    /**
     * 身高标准级别
     */
    enum class HeightLevel {
        SHORT,      // 矮小
        BELOW_AVG,  // 偏矮
        STANDARD,   // 标准
        TALL        // 超高
    }

    /**
     * 体重标准级别
     */
    enum class WeightLevel {
        UNDERWEIGHT, // 偏瘦
        STANDARD,    // 标准
        OVERWEIGHT,  // 超重
        OBESE        // 肥胖
    }

    /**
     * 成长分析结果
     */
    data class AnalysisResult(
        val ageInMonths: Int,
        val heightLevel: HeightLevel?,
        val heightPercentile: String?, // 百分比描述
        val weightLevel: WeightLevel?,
        val weightPercentile: String?, // 百分比描述
        val heightSuggestion: String?,
        val weightSuggestion: String?
    )

    // 男孩身高标准数据 (cm) - 1-18岁
    // 顺序：矮小、偏矮、标准、超高
    private val boyHeightStandards = mapOf(
        1 to listOf(71.2f, 73.8f, 76.5f, 79.3f),
        2 to listOf(81.6f, 85.1f, 88.5f, 92.1f),
        3 to listOf(89.3f, 93.0f, 96.8f, 100.7f),
        4 to listOf(96.3f, 100.2f, 104.1f, 108.2f),
        5 to listOf(102.8f, 107.0f, 111.3f, 115.7f),
        6 to listOf(108.6f, 113.1f, 117.7f, 122.4f),
        7 to listOf(114.0f, 119.0f, 124.0f, 129.1f),
        8 to listOf(119.3f, 124.6f, 130.0f, 135.5f),
        9 to listOf(123.9f, 129.6f, 135.4f, 147.2f),
        10 to listOf(127.9f, 134.0f, 140.2f, 146.4f),
        11 to listOf(132.1f, 138.7f, 145.3f, 152.1f),
        12 to listOf(137.2f, 144.6f, 151.9f, 159.4f),
        13 to listOf(144.0f, 151.8f, 159.5f, 167.3f),
        14 to listOf(151.5f, 158.7f, 165.9f, 173.1f),
        15 to listOf(156.7f, 163.3f, 169.8f, 176.3f),
        16 to listOf(159.1f, 165.4f, 171.6f, 177.8f),
        17 to listOf(160.1f, 166.3f, 172.3f, 178.4f),
        18 to listOf(160.5f, 166.6f, 172.7f, 178.7f)
    )

    // 男孩体重标准数据 (kg) - 1-18岁
    // 顺序：偏瘦、标准、超重、肥胖
    private val boyWeightStandards = mapOf(
        1 to listOf(9.0f, 10.05f, 11.23f, 12.54f),
        2 to listOf(11.24f, 12.54f, 14.01f, 15.37f),
        3 to listOf(13.13f, 14.65f, 16.39f, 18.37f),
        4 to listOf(14.88f, 16.64f, 18.67f, 21.01f),
        5 to listOf(16.87f, 18.98f, 21.46f, 24.38f),
        6 to listOf(18.71f, 21.26f, 24.32f, 28.03f),
        7 to listOf(20.83f, 24.06f, 28.05f, 33.08f),
        8 to listOf(23.23f, 27.33f, 32.57f, 39.41f),
        9 to listOf(25.50f, 30.46f, 36.92f, 45.52f),
        10 to listOf(27.93f, 33.74f, 41.31f, 51.38f),
        11 to listOf(30.95f, 37.69f, 46.33f, 57.58f),
        12 to listOf(34.67f, 42.49f, 52.31f, 64.68f),
        13 to listOf(39.22f, 48.08f, 59.04f, 72.60f),
        14 to listOf(44.08f, 53.37f, 64.84f, 79.07f),
        15 to listOf(48.0f, 57.08f, 68.35f, 82.45f),
        16 to listOf(50.62f, 59.35f, 70.20f, 83.85f),
        17 to listOf(52.20f, 60.68f, 71.20f, 84.45f),
        18 to listOf(53.08f, 61.40f, 71.73f, 84.73f)
    )

    // 女孩身高标准数据 (cm) - 1-18岁
    private val girlHeightStandards = mapOf(
        1 to listOf(69.7f, 72.3f, 75.0f, 77.7f),
        2 to listOf(80.5f, 83.8f, 87.2f, 90.7f),
        3 to listOf(88.2f, 91.8f, 95.6f, 99.4f),
        4 to listOf(95.4f, 99.2f, 103.1f, 107.0f),
        5 to listOf(101.8f, 106.0f, 110.2f, 114.5f),
        6 to listOf(107.6f, 112.0f, 116.6f, 121.2f),
        7 to listOf(112.7f, 117.6f, 122.5f, 127.6f),
        8 to listOf(117.9f, 123.1f, 128.5f, 133.9f),
        9 to listOf(122.6f, 128.3f, 134.1f, 139.9f),
        10 to listOf(127.6f, 133.8f, 140.1f, 146.4f),
        11 to listOf(133.4f, 140.0f, 146.6f, 153.3f),
        12 to listOf(135.4f, 143.0f, 148.9f, 156.4f),
        13 to listOf(139.5f, 145.9f, 152.4f, 158.8f),
        14 to listOf(147.2f, 152.9f, 158.6f, 164.3f),
        15 to listOf(148.8f, 154.3f, 159.8f, 165.3f),
        16 to listOf(149.2f, 154.7f, 160.1f, 165.5f),
        17 to listOf(149.5f, 154.9f, 160.3f, 165.7f),
        18 to listOf(149.8f, 155.2f, 160.6f, 165.9f)
    )

    // 女孩体重标准数据 (kg) - 1-18岁
    private val girlWeightStandards = mapOf(
        1 to listOf(8.45f, 9.4f, 10.48f, 11.73f),
        2 to listOf(10.7f, 11.92f, 13.31f, 14.92f),
        3 to listOf(12.65f, 14.13f, 15.83f, 17.81f),
        4 to listOf(14.44f, 16.17f, 18.19f, 20.54f),
        5 to listOf(16.2f, 18.26f, 20.66f, 23.50f),
        6 to listOf(17.94f, 20.37f, 23.27f, 26.74f),
        7 to listOf(19.74f, 22.64f, 26.16f, 30.45f),
        8 to listOf(21.75f, 25.25f, 29.56f, 34.94f),
        9 to listOf(23.96f, 28.19f, 33.51f, 40.32f),
        10 to listOf(26.60f, 31.76f, 38.41f, 47.15f),
        11 to listOf(29.99f, 36.10f, 44.09f, 54.78f),
        12 to listOf(31.48f, 38.6f, 46.7f, 58.59f),
        13 to listOf(34.04f, 40.77f, 49.54f, 61.22f),
        14 to listOf(41.18f, 47.83f, 56.61f, 66.77f),
        15 to listOf(43.42f, 49.82f, 57.72f, 67.61f),
        16 to listOf(44.56f, 50.81f, 58.45f, 67.93f),
        17 to listOf(45.01f, 51.20f, 58.73f, 68.04f),
        18 to listOf(45.26f, 51.41f, 58.88f, 68.10f)
    )

    /**
     * 分析成长数据
     *
     * @param gender 性别："男" 或 "女"
     * @param ageInYears 年龄（岁）
     * @param heightCm 身高（厘米），可为null
     * @param weightKg 体重（公斤），可为null
     * @return 分析结果
     */
    fun analyze(
        gender: String,
        ageInYears: Int,
        heightCm: Float?,
        weightKg: Float?
    ): AnalysisResult {
        val clampedAge = ageInYears.coerceIn(1, 18)
        val ageInMonths = clampedAge * 12

        val isBoy = gender == "男"
        val heightStandards = if (isBoy) boyHeightStandards else girlHeightStandards
        val weightStandards = if (isBoy) boyWeightStandards else girlWeightStandards

        // 分析身高
        val heightAnalysis = heightCm?.let { analyzeHeight(it, heightStandards[clampedAge]!!) }

        // 分析体重
        val weightAnalysis = weightKg?.let { analyzeWeight(it, weightStandards[clampedAge]!!) }

        return AnalysisResult(
            ageInMonths = ageInMonths,
            heightLevel = heightAnalysis?.first,
            heightPercentile = heightAnalysis?.second,
            weightLevel = weightAnalysis?.first,
            weightPercentile = weightAnalysis?.second,
            heightSuggestion = generateHeightSuggestion(heightAnalysis?.first),
            weightSuggestion = generateWeightSuggestion(weightAnalysis?.first)
        )
    }

    /**
     * 分析身高
     */
    private fun analyzeHeight(
        height: Float,
        standards: List<Float>
    ): Pair<HeightLevel, String> {
        return when {
            height < standards[0] -> HeightLevel.SHORT to "< 3% (矮小)"
            height < standards[1] -> HeightLevel.BELOW_AVG to "3-25% (偏矮)"
            height < standards[2] -> HeightLevel.STANDARD to "25-50% (标准偏下)"
            height <= standards[3] -> HeightLevel.STANDARD to "50-97% (标准偏上)"
            else -> HeightLevel.TALL to "> 97% (超高)"
        }
    }

    /**
     * 分析体重
     */
    private fun analyzeWeight(
        weight: Float,
        standards: List<Float>
    ): Pair<WeightLevel, String> {
        return when {
            weight < standards[0] -> WeightLevel.UNDERWEIGHT to "< 3% (偏瘦)"
            weight < standards[1] -> WeightLevel.STANDARD to "3-50% (标准偏轻)"
            weight <= standards[2] -> WeightLevel.STANDARD to "50-85% (标准)"
            weight <= standards[3] -> WeightLevel.OVERWEIGHT to "85-97% (超重)"
            else -> WeightLevel.OBESE to "> 97% (肥胖)"
        }
    }

    /**
     * 生成身高建议
     */
    private fun generateHeightSuggestion(level: HeightLevel?): String? {
        return when (level) {
            HeightLevel.SHORT -> "身高偏矮，建议咨询儿科医生，检查营养和生长激素情况"
            HeightLevel.BELOW_AVG -> "身高略低于平均水平，注意均衡营养和充足睡眠"
            HeightLevel.STANDARD -> "身高发育良好，继续保持健康的生活习惯"
            HeightLevel.TALL -> "身高发育优秀，基因优势或发育较早"
            null -> null
        }
    }

    /**
     * 生成体重建议
     */
    private fun generateWeightSuggestion(level: WeightLevel?): String? {
        return when (level) {
            WeightLevel.UNDERWEIGHT -> "体重偏轻，注意增加营养摄入，避免挑食"
            WeightLevel.STANDARD -> "体重正常，保持均衡饮食和适量运动"
            WeightLevel.OVERWEIGHT -> "体重偏重，注意控制饮食，增加运动量"
            WeightLevel.OBESE -> "体重超标，建议咨询医生，制定减重计划"
            null -> null
        }
    }

    /**
     * 计算年龄（岁）
     */
    fun calculateAgeInYears(birthday: java.time.LocalDate?, recordDate: java.time.LocalDate): Int? {
        if (birthday == null) return null
        return java.time.Period.between(birthday, recordDate).years.coerceAtLeast(0)
    }

    /**
     * 获取指定年龄和性别的身高标准数据
     * @param gender 性别："男" 或 "女"
     * @param ageInYears 年龄（岁），1-18
     * @return 四个阈值：[矮小, 偏矮, 标准, 超高]，如果年龄无效返回null
     */
    fun getHeightStandards(gender: String, ageInYears: Int): List<Float>? {
        val clampedAge = ageInYears.coerceIn(1, 18)
        val isBoy = gender == "男"
        val standards = if (isBoy) boyHeightStandards else girlHeightStandards
        return standards[clampedAge]
    }

    /**
     * 获取指定年龄和性别的体重标准数据
     * @param gender 性别："男" 或 "女"
     * @param ageInYears 年龄（岁），1-18
     * @return 四个阈值：[偏瘦, 标准, 超重, 肥胖]，如果年龄无效返回null
     */
    fun getWeightStandards(gender: String, ageInYears: Int): List<Float>? {
        val clampedAge = ageInYears.coerceIn(1, 18)
        val isBoy = gender == "男"
        val standards = if (isBoy) boyWeightStandards else girlWeightStandards
        return standards[clampedAge]
    }

    /**
     * 获取所有年龄段的身高标准数据
     * @param gender 性别："男" 或 "女"
     * @return Map<年龄, [矮小, 偏矮, 标准, 超高]>
     */
    fun getAllHeightStandards(gender: String): Map<Int, List<Float>> {
        val isBoy = gender == "男"
        return if (isBoy) boyHeightStandards else girlHeightStandards
    }

    /**
     * 获取所有年龄段的体重标准数据
     * @param gender 性别："男" 或 "女"
     * @return Map<年龄, [偏瘦, 标准, 超重, 肥胖]>
     */
    fun getAllWeightStandards(gender: String): Map<Int, List<Float>> {
        val isBoy = gender == "男"
        return if (isBoy) boyWeightStandards else girlWeightStandards
    }

    /**
     * 计算完整年龄文本（xx岁xx天格式）
     * 1岁以下显示总天数，1岁以上显示X岁X天，为0时省略
     */
    fun calculateAgeText(birthday: java.time.LocalDate?): String {
        if (birthday == null) return "生日未设置"
        val today = java.time.LocalDate.now()
        val period = java.time.Period.between(birthday, today)
        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(birthday, today).toInt()

        return when {
            period.years > 0 -> {
                // 计算今年的已过天数
                val currentYearBirthday = birthday.plusYears(period.years.toLong())
                val daysThisYear = java.time.temporal.ChronoUnit.DAYS.between(currentYearBirthday, today).toInt()
                if (daysThisYear > 0) "${period.years}岁${daysThisYear}天" else "${period.years}岁"
            }
            totalDays > 0 -> "${totalDays}天"
            else -> "刚出生"
        }
    }
}
