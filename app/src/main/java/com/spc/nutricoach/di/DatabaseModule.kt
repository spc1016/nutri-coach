package com.spc.nutricoach.di

import android.content.Context
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.data.local.AppDatabase
import com.spc.nutricoach.data.local.dao.NotasDao
import com.spc.nutricoach.data.local.dao.PesoHistorialDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideNotasDao(database: AppDatabase): NotasDao {
        return database.notasDao()
    }

    @Provides
    @Singleton
    fun providePesoHistorialDao(database: AppDatabase): PesoHistorialDao {
        return database.pesoHistorialDao()
    }

    @Provides
    @Singleton
    fun provideDietTrackerManager(@ApplicationContext context: Context): com.spc.nutricoach.data.DietTrackerManager {
        return com.spc.nutricoach.data.DietTrackerManager(context)
    }

    @Provides
    @Singleton
    fun provideRoutineTrackerManager(@ApplicationContext context: Context): com.spc.nutricoach.data.RoutineTrackerManager {
        return com.spc.nutricoach.data.RoutineTrackerManager(context)
    }
}
