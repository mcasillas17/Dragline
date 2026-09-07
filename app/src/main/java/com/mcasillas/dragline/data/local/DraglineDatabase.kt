package com.mcasillas.dragline.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mcasillas.dragline.data.local.dao.AlarmDao
import com.mcasillas.dragline.data.local.dao.PlayedTrackDao
import com.mcasillas.dragline.data.local.entity.AlarmEntity
import com.mcasillas.dragline.data.local.entity.PlayedTrackEntity

@Database(
    entities = [
        AlarmEntity::class,
        PlayedTrackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DraglineDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun playedTrackDao(): PlayedTrackDao
}
