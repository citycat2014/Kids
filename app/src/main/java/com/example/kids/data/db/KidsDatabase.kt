package com.example.kids.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kids.data.dao.GrowthRecordDao
import com.example.kids.data.dao.KidDao
import com.example.kids.data.dao.MoodRecordDao
import com.example.kids.data.model.GrowthRecordEntity
import com.example.kids.data.model.KidEntity
import com.example.kids.data.model.MoodRecordEntity

@Database(
    entities = [
        KidEntity::class,
        GrowthRecordEntity::class,
        MoodRecordEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KidsDatabase : RoomDatabase() {

    abstract fun kidDao(): KidDao
    abstract fun growthRecordDao(): GrowthRecordDao
    abstract fun moodRecordDao(): MoodRecordDao

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

        fun getInstance(context: Context): KidsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KidsDatabase::class.java,
                    "kids.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}