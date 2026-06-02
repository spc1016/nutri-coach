package com.spc.nutricoach.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spc.nutricoach.data.local.dao.NotasDao
import com.spc.nutricoach.data.local.dao.PesoHistorialDao
import com.spc.nutricoach.data.local.entity.NotasEntity
import com.spc.nutricoach.data.local.entity.PesoHistorialEntity

@Database(entities = [NotasEntity::class, PesoHistorialEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun notasDao(): NotasDao
    abstract fun pesoHistorialDao(): PesoHistorialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Mapeo manual del cambio estructural introducido en la versión 2
                db.execSQL("ALTER TABLE notas ADD COLUMN clienteId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear la tabla para el historial de pesos de ejercicios
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `peso_historial` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `clienteId` TEXT NOT NULL, 
                        `rutinaId` TEXT NOT NULL, 
                        `exerciseKey` TEXT NOT NULL, 
                        `peso` TEXT NOT NULL, 
                        `fecha` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notas_app_bd"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
