package com.example.kids.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kids.data.dao.AcademicRecordDao
import com.example.kids.data.dao.ExerciseRecordDao
import com.example.kids.data.dao.GrowthRecordDao
import com.example.kids.data.dao.KidDao
import com.example.kids.data.dao.MoodRecordDao
import com.example.kids.data.model.AcademicRecordEntity
import com.example.kids.data.model.ExerciseRecordEntity
import com.example.kids.data.model.GrowthRecordEntity
import com.example.kids.data.model.KidEntity
import com.example.kids.data.model.MoodRecordEntity

@Database(
    entities = [
        KidEntity::class,
        GrowthRecordEntity::class,
        MoodRecordEntity::class,
        ExerciseRecordEntity::class,
        AcademicRecordEntity::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KidsDatabase : RoomDatabase() {

    abstract fun kidDao(): KidDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun moodRecordDao(): MoodRecordDao
    abstract fun exerciseRecordDao(): ExerciseRecordDao
    abstract fun academicRecordDao(): AcademicRecordDao

    companion object {
        @Volatile
        private var INSTANCE: KidsDatabase? = null

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE growth_records ADD COLUMN latitude REAL")
                database.execSQL("ALTER TABLE growth_records ADD COLUMN longitude REAL")
            }
        }

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE mood_records ADD COLUMN exerciseType TEXT")
                database.execSQL("ALTER TABLE mood_records ADD COLUMN exerciseDurationMinutes INTEGER")
            }
        }

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建 exercise_records 表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercise_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        moodRecordId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        FOREIGN KEY(moodRecordId) REFERENCES mood_records(id) ON DELETE CASCADE
                    )
                """)
                // 迁移现有运动数据到新表
                database.execSQL("""
                    INSERT INTO exercise_records (moodRecordId, type, durationMinutes)
                    SELECT id, exerciseType, exerciseDurationMinutes
                    FROM mood_records
                    WHERE exerciseType IS NOT NULL AND exerciseDurationMinutes IS NOT NULL
                """)
            }
        }

        // 学习档案表迁移（5→6）
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS academic_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        kidId INTEGER NOT NULL,
                        examName TEXT NOT NULL,
                        examDate INTEGER NOT NULL,
                        examType TEXT NOT NULL,
                        gradeLevel TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        score REAL,
                        grade TEXT,
                        comment TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)
                database.execSQL("CREATE INDEX index_academic_records_kidId ON academic_records(kidId)")
                database.execSQL("CREATE INDEX index_academic_records_examDate ON academic_records(examDate)")
            }
        }

        // Kid表增加年级字段迁移（6→7）
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE kids ADD COLUMN gradeLevel TEXT")
                database.execSQL("ALTER TABLE kids ADD COLUMN gradeOffset INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE kids ADD COLUMN gradeAutoCalculate INTEGER NOT NULL DEFAULT 1")
            }
        }

        // 修复 academic_records 表 examDate 字段类型（7→8）
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. 将旧表重命名为临时表
                database.execSQL("ALTER TABLE academic_records RENAME TO academic_records_old")

                // 2. 创建新表，使用正确的 INTEGER 类型存储 examDate
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS academic_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        kidId INTEGER NOT NULL,
                        examName TEXT NOT NULL,
                        examDate INTEGER NOT NULL,
                        examType TEXT NOT NULL,
                        gradeLevel TEXT NOT NULL,
                        subject TEXT NOT NULL,
                        score REAL,
                        grade TEXT,
                        comment TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // 3. 尝试迁移旧数据（如果存在）
                try {
                    database.execSQL("""
                        INSERT INTO academic_records (id, kidId, examName, examDate, examType, gradeLevel, subject, score, grade, comment, createdAt)
                        SELECT
                            id,
                            kidId,
                            examName,
                            CASE
                                WHEN examDate IS NULL THEN 0
                                WHEN examDate GLOB '*[^0-9-]*' THEN 0
                                ELSE CAST(examDate AS INTEGER)
                            END,
                            examType,
                            gradeLevel,
                            subject,
                            score,
                            grade,
                            comment,
                            createdAt
                        FROM academic_records_old
                    """)
                } catch (e: Exception) {
                    // 如果迁移失败，则忽略
                }

                // 4. 删除临时表
                database.execSQL("DROP TABLE IF EXISTS academic_records_old")

                // 5. 创建索引
                database.execSQL("CREATE INDEX index_academic_records_kidId ON academic_records(kidId)")
                database.execSQL("CREATE INDEX index_academic_records_examDate ON academic_records(examDate)")
            }
        }

        // 8→9 空迁移（Entity 已添加索引定义，无需实际操作）
        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Entity 现在定义了索引，但实际表结构不需要改变
                // 这个迁移只是为了版本号同步
            }
        }

        // 9→10 添加学期字段
        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE academic_records ADD COLUMN semester TEXT NOT NULL DEFAULT '上学期'")
            }
        }

        // 10→11 添加附加分字段
        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE academic_records ADD COLUMN bonusScore REAL")
            }
        }

        fun getInstance(context: Context): KidsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KidsDatabase::class.java,
                    "kids.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}