package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [LevelProgressEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun levelProgressDao(): LevelProgressDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "color_worlds_game.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch(Dispatchers.IO) {
                            prepopulateDatabase(getDatabase(context, scope).levelProgressDao())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun prepopulateDatabase(dao: LevelProgressDao) {
            val initialList = (1..50).map { levelNum ->
                LevelProgressEntity(
                    levelNumber = levelNum,
                    stars = 0,
                    highScore = 0,
                    isUnlocked = levelNum == 1,
                    isCompleted = false
                )
            }
            dao.insertAll(initialList)
        }
    }
}
