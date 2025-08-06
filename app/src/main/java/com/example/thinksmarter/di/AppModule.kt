package com.example.thinksmarter.di

import android.content.Context
import com.example.thinksmarter.data.db.AppDatabase
import com.example.thinksmarter.data.repository.ThinkSmarterRepositoryImpl
import com.example.thinksmarter.domain.repository.ThinkSmarterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.thinksmarter.data.auth.UserAuthManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideUserAuthManager(@ApplicationContext context: Context): UserAuthManager {
        return UserAuthManager(context)
    }

    @Provides
    @Singleton
    fun provideThinkSmarterRepository(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): ThinkSmarterRepository {
        return ThinkSmarterRepositoryImpl(context, database)
    }
}
