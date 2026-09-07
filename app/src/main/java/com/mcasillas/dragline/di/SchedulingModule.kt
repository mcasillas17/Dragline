package com.mcasillas.dragline.di

import com.mcasillas.dragline.alarm.AlarmScheduler
import com.mcasillas.dragline.alarm.AndroidAlarmScheduler
import com.mcasillas.dragline.scheduling.DefaultMeetingScheduleCalculator
import com.mcasillas.dragline.scheduling.DefaultSunriseScheduleCalculator
import com.mcasillas.dragline.scheduling.FixedTimeCalculator
import com.mcasillas.dragline.scheduling.MeetingScheduleCalculator
import com.mcasillas.dragline.scheduling.SunriseScheduleCalculator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulingModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AndroidAlarmScheduler): AlarmScheduler

    companion object {
        @Provides
        @Singleton
        fun provideFixedTimeCalculator(): FixedTimeCalculator = FixedTimeCalculator()

        @Provides
        @Singleton
        fun provideSunriseCalculator(): SunriseScheduleCalculator = DefaultSunriseScheduleCalculator()

        @Provides
        @Singleton
        fun provideMeetingCalculator(): MeetingScheduleCalculator = DefaultMeetingScheduleCalculator()
    }
}
