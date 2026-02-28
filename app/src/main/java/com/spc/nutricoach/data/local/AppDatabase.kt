package com.spc.nutricoach.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spc.nutricoach.data.local.dao.NotasDao
import com.spc.nutricoach.data.local.entity.NotasEntity

@Database(entities = [NotasEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notasDao(): NotasDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notas_app_bd"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
