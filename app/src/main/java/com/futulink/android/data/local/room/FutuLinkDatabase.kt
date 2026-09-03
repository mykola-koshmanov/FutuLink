package com.futulink.android.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MeasurementEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FutuLinkDatabase : RoomDatabase() {

    abstract fun measurementDao(): MeasurementDao

    companion object {
        const val NAME = "futulink.db"
    }
}
