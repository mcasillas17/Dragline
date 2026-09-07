package com.mcasillas.dragline.di

import android.content.Context
import androidx.room.Room
import com.mcasillas.dragline.data.local.DraglineDatabase
import com.mcasillas.dragline.data.local.dao.AlarmDao
import com.mcasillas.dragline.data.local.dao.PlayedTrackDao
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
    fun provideDatabase(@ApplicationContext context: Context): DraglineDatabase {
        return Room.databaseBuilder(
            context,
            DraglineDatabase::class.java,
            "dragline.db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideAlarmDao(database: DraglineDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    fun providePlayedTrackDao(database: DraglineDatabase): PlayedTrackDao {
        return database.playedTrackDao()
    }
}
