package com.example.kids.ui.academic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kids.data.db.KidsDatabase
import com.example.kids.data.model.AcademicRecordEntity
import com.example.kids.data.model.SubjectConfig
import com.example.kids.data.repository.AcademicRecordRepository
import com.example.kids.data.repository.KidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.LocalDate

// UI数据类
data class AcademicRecordUi(
    val id: Long,
    val examName: String,
    val examDate: LocalDate,
    val examType: String,
    val gradeLevel: String,
    val subject: String,
    val score: Float?,              // 主分数
    val bonusScore: Float?,         // 附加分
    val grade: String?,
    val comment: String?
)

data class ExamUi(
    val examName: String,
    val examDate: LocalDate,
    val examType: String,
    val gradeLevel: String,
    val records: List<AcademicRecordUi>,
    val averageScore: Float?
)

// 科目成绩输入数据类
data class SubjectScoreInput(
    val subject: String,
    val score: Float?,              // 主分数
    val bonusScore: Float?,         // 附加分（可选）
    val grade: String?,
    val comment: String?,
    val gradeManuallySet: Boolean = false
)

data class AcademicRecordUiState(
    val kidId: Long = 0L,
    val kidName: String = "",
    val kidGrade: String = "",
    val exams: List<ExamUi> = emptyList(),

    // 编辑模式状态
    val selectedEditGrade: String = "",
    val selectedEditSemester: String = "上学期",
    val availableGrades: List<String> = SubjectConfig.getAllGrades(),

    // 预览模式状态
    val selectedPreviewGrade: String = "",
    val selectedPreviewSemester: String = "上学期",
    val chartData: List<ChartDataPoint> = emptyList(),
    val subjectStatistics: List<SubjectStatistics> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AcademicRecordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AcademicRecordRepository(KidsDatabase.getInstance(application).academicRecordDao())
    private val kidRepository = KidRepository(KidsDatabase.getInstance(application).kidDao())

    private val _uiState = MutableStateFlow(AcademicRecordUiState())
    val uiState: StateFlow<AcademicRecordUiState> = _uiState.asStateFlow()

    fun load(kidId: Long, initialGrade: String? = null) {
        _uiState.value = _uiState.value.copy(kidId = kidId, isLoading = true)

        viewModelScope.launch {
            // Load kid info
            kidRepository.observeKid(kidId)
                .filterNotNull()
                .collectLatest { kid ->
                    val kidGrade = kid.gradeLevel ?: ""
                    val currentSemester = SemesterHelper.getCurrentSemester()

                    // 优先使用传入的initialGrade，否则使用数据库中的年级
                    val effectiveGrade = if (!initialGrade.isNullOrBlank()) {
                        initialGrade
                    } else {
                        kidGrade
                    }

                    _uiState.value = _uiState.value.copy(
                        kidName = kid.name,
                        kidGrade = kidGrade,
                        selectedEditGrade = effectiveGrade,
                        selectedEditSemester = currentSemester,
                        selectedPreviewGrade = effectiveGrade,
                        selectedPreviewSemester = currentSemester
                    )
                    // Load data for the effective grade and current semester
                    loadGradeData(effectiveGrade, currentSemester)
                }
        }
    }

    /**
     * 选择编辑模式的年级
     */
    fun selectEditGrade(grade: String) {
        _uiState.value = _uiState.value.copy(selectedEditGrade = grade)
        loadGradeData(grade, _uiState.value.selectedEditSemester)
    }

    /**
     * 选择编辑模式的学期
     */
    fun selectEditSemester(semester: String) {
        _uiState.value = _uiState.value.copy(selectedEditSemester = semester)
        loadGradeData(_uiState.value.selectedEditGrade, semester)
    }

    /**
     * 选择预览模式的年级
     */
    fun selectPreviewGrade(grade: String) {
        _uiState.value = _uiState.value.copy(selectedPreviewGrade = grade)
        loadGradeData(grade, _uiState.value.selectedPreviewSemester)
    }

    /**
     * 选择预览模式的学期
     */
    fun selectPreviewSemester(semester: String) {
        _uiState.value = _uiState.value.copy(selectedPreviewSemester = semester)
        loadGradeData(_uiState.value.selectedPreviewGrade, semester)
    }

    /**
     * 加载指定年级和学期的数据
     */
    private fun loadGradeData(grade: String, semester: String) {
        viewModelScope.launch {
            val kidId = _uiState.value.kidId

            if (grade.isEmpty()) {
                repository.observeRecordsByKid(kidId)
                    .collectLatest { records ->
                        updateStateWithRecords(records)
                    }
            } else {
                repository.observeRecordsByKidGradeAndSemester(kidId, grade, semester)
                    .collectLatest { records ->
                        updateStateWithRecords(records)
                    }
            }
        }
    }

    /**
     * 更新状态并计算统计数据
     */
    private fun updateStateWithRecords(records: List<AcademicRecordEntity>) {
        val exams = groupRecordsByExam(records)
        val chartData = calculateChartData(records)
        val subjectStats = calculateSubjectStatistics(records)

        _uiState.value = _uiState.value.copy(
            exams = exams,
            chartData = chartData,
            subjectStatistics = subjectStats,
            isLoading = false
        )
    }

    /**
     * 判断是否小学（用于科目过滤）
     */
    private fun isPrimarySchool(records: List<AcademicRecordEntity>): Boolean {
        return records.firstOrNull()?.gradeLevel?.contains("小学") == true
    }

    /**
     * 过滤小学科目（仅保留语数英）
     */
    private fun filterPrimarySubjects(records: List<AcademicRecordEntity>): List<AcademicRecordEntity> {
        if (!isPrimarySchool(records)) return records
        val primarySubjects = listOf("语文", "数学", "英语")
        return records.filter { it.subject in primarySubjects }
    }

    /**
     * 计算图表数据点
     */
    private fun calculateChartData(records: List<AcademicRecordEntity>): List<ChartDataPoint> {
        return filterPrimarySubjects(records)
            .filter { it.score != null }
            .map { record ->
                ChartDataPoint(
                    date = record.examDate,
                    subject = record.subject,
                    score = record.score!!,
                    examType = record.examType,
                    examName = record.examName
                )
            }
            .sortedBy { it.date }
    }

    /**
     * 计算各科目统计数据
     */
    private fun calculateSubjectStatistics(records: List<AcademicRecordEntity>): List<SubjectStatistics> {
        val recordsWithScore = filterPrimarySubjects(records).filter { it.score != null }

        return recordsWithScore
            .groupBy { it.subject }
            .map { (subject, subjectRecords) ->
                val scores = subjectRecords.map { it.score!! }
                val average = scores.average().toFloat()
                val highest = scores.maxOrNull() ?: 0f
                val lowest = scores.minOrNull() ?: 0f

                // 计算趋势（最近3次 vs 之前3次）
                val sortedRecords = subjectRecords.sortedBy { it.examDate }
                val trend = calculateTrend(sortedRecords)

                SubjectStatistics(
                    subject = subject,
                    averageScore = average,
                    highestScore = highest,
                    lowestScore = lowest,
                    totalExams = subjectRecords.size,
                    trend = trend
                )
            }
    }

    /**
     * 计算趋势方向
     */
    private fun calculateTrend(records: List<AcademicRecordEntity>): TrendDirection {
        if (records.size < 4) return TrendDirection.STABLE

        val recentScores = records.takeLast(3).map { it.score ?: 0f }
        val previousScores = records.dropLast(3).takeLast(3).map { it.score ?: 0f }

        if (previousScores.isEmpty()) return TrendDirection.STABLE

        val recentAvg = recentScores.average()
        val previousAvg = previousScores.average()
        val diff = recentAvg - previousAvg

        return when {
            diff > 3 -> TrendDirection.UP
            diff < -3 -> TrendDirection.DOWN
            else -> TrendDirection.STABLE
        }
    }

    /**
     * 获取指定科目的图表数据（按日期排序）
     */
    fun getSubjectChartData(subject: String): List<ChartDataPoint> {
        return _uiState.value.chartData
            .filter { it.subject == subject }
            .sortedBy { it.date }
    }

    /**
     * 获取所有科目列表
     */
    fun getAvailableSubjects(): List<String> {
        return _uiState.value.chartData
            .map { it.subject }
            .distinct()
            .sorted()
    }

    fun saveExam(
        examType: String,
        examDate: LocalDate,
        gradeLevel: String,
        semester: String,
        records: List<SubjectScoreInput>
    ) {
        viewModelScope.launch {
            try {
                val kidId = _uiState.value.kidId
                val examName = SubjectConfig.generateExamName(examType)

                val entities = records.filter { it.score != null || !it.grade.isNullOrBlank() }
                    .map { input ->
                        AcademicRecordEntity(
                            kidId = kidId,
                            examName = examName,
                            examDate = examDate,
                            examType = examType,
                            gradeLevel = gradeLevel,
                            semester = semester,
                            subject = input.subject,
                            score = input.score,
                            bonusScore = input.bonusScore,
                            grade = input.grade,
                            comment = input.comment
                        )
                    }

                if (entities.isNotEmpty()) {
                    repository.saveExamRecords(entities)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "保存失败: ${e.message}")
            }
        }
    }

    fun saveSingleRecord(
        examType: String,
        examDate: LocalDate,
        gradeLevel: String,
        semester: String,
        unit: String?,
        record: SubjectScoreInput
    ) {
        viewModelScope.launch {
            try {
                val kidId = _uiState.value.kidId
                val examName = SubjectConfig.generateExamName(examType, unit, record.subject)

                val entity = AcademicRecordEntity(
                    kidId = kidId,
                    examName = examName,
                    examDate = examDate,
                    examType = examType,
                    gradeLevel = gradeLevel,
                    semester = semester,
                    subject = record.subject,
                    score = record.score,
                    bonusScore = record.bonusScore,
                    grade = record.grade,
                    comment = record.comment
                )
                repository.saveRecord(entity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "保存失败: ${e.message}")
            }
        }
    }

    fun deleteExam(date: LocalDate, examName: String) {
        viewModelScope.launch {
            try {
                repository.deleteExam(_uiState.value.kidId, date, examName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "删除失败: ${e.message}")
            }
        }
    }

    fun updateExam(
        oldDate: LocalDate,
        oldExamName: String,
        examType: String,
        examDate: LocalDate,
        gradeLevel: String,
        semester: String,
        records: List<SubjectScoreInput>
    ) {
        viewModelScope.launch {
            try {
                val kidId = _uiState.value.kidId
                // 先删除旧记录
                repository.deleteExam(kidId, oldDate, oldExamName)
                // 再保存新记录
                val examName = SubjectConfig.generateExamName(examType)
                val entities = records.filter { it.score != null || !it.grade.isNullOrBlank() }
                    .map { input ->
                        AcademicRecordEntity(
                            kidId = kidId,
                            examName = examName,
                            examDate = examDate,
                            examType = examType,
                            gradeLevel = gradeLevel,
                            semester = semester,
                            subject = input.subject,
                            score = input.score,
                            bonusScore = input.bonusScore,
                            grade = input.grade,
                            comment = input.comment
                        )
                    }
                if (entities.isNotEmpty()) {
                    repository.saveExamRecords(entities)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "更新失败: ${e.message}")
            }
        }
    }

    fun updateSingleRecord(
        oldDate: LocalDate,
        oldExamName: String,
        examType: String,
        examDate: LocalDate,
        gradeLevel: String,
        semester: String,
        unit: String?,
        record: SubjectScoreInput
    ) {
        viewModelScope.launch {
            try {
                val kidId = _uiState.value.kidId
                // 先删除旧记录
                repository.deleteExam(kidId, oldDate, oldExamName)
                // 再保存新记录
                val examName = SubjectConfig.generateExamName(examType, unit, record.subject)
                val entity = AcademicRecordEntity(
                    kidId = kidId,
                    examName = examName,
                    examDate = examDate,
                    examType = examType,
                    gradeLevel = gradeLevel,
                    semester = semester,
                    subject = record.subject,
                    score = record.score,
                    bonusScore = record.bonusScore,
                    grade = record.grade,
                    comment = record.comment
                )
                repository.saveRecord(entity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "更新失败: ${e.message}")
            }
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteRecord(recordId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "删除失败: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun groupRecordsByExam(records: List<AcademicRecordEntity>): List<ExamUi> {
        // 过滤小学科目
        val filteredRecords = filterPrimarySubjects(records)

        return filteredRecords.groupBy { Triple(it.examDate, it.examName, it.examType) }
            .map { (key, recordList) ->
                val (date, name, type) = key
                val uiRecords = recordList.map { it.toUi() }
                val averageScore = uiRecords
                    .mapNotNull { it.score }
                    .takeIf { it.isNotEmpty() }
                    ?.average()
                    ?.toFloat()

                ExamUi(
                    examName = name,
                    examDate = date,
                    examType = type,
                    gradeLevel = recordList.firstOrNull()?.gradeLevel ?: "",
                    records = uiRecords,
                    averageScore = averageScore
                )
            }
            .sortedByDescending { it.examDate }
    }

    private fun AcademicRecordEntity.toUi(): AcademicRecordUi {
        return AcademicRecordUi(
            id = id,
            examName = examName,
            examDate = examDate,
            examType = examType,
            gradeLevel = gradeLevel,
            subject = subject,
            score = score,
            bonusScore = bonusScore,
            grade = grade,
            comment = comment
        )
    }
}
