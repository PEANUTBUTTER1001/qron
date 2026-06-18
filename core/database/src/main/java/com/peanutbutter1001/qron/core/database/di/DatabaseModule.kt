package com.peanutbutter1001.qron.core.database.di

import android.content.Context
import androidx.room.Room
import com.peanutbutter1001.qron.core.database.local.AppDatabase
import com.peanutbutter1001.qron.core.database.local.HistoryDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "qron_database"
        ).fallbackToDestructiveMigration().build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DaosModule {
    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }
}
