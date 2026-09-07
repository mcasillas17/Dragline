package com.mcasillas.dragline.di

import com.mcasillas.dragline.data.repository.AlarmRepositoryImpl
import com.mcasillas.dragline.data.repository.SettingsRepositoryImpl
import com.mcasillas.dragline.data.repository.TrackHistoryRepositoryImpl
import com.mcasillas.dragline.domain.repository.AlarmRepository
import com.mcasillas.dragline.domain.repository.SettingsRepository
import com.mcasillas.dragline.domain.repository.TrackHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTrackHistoryRepository(impl: TrackHistoryRepositoryImpl): TrackHistoryRepository
}
