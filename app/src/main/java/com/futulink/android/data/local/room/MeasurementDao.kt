package com.futulink.android.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {

    @Insert
    suspend fun insert(measurement: MeasurementEntity)

    /** Room re-emits this flow whenever the table changes, which drives the Statistics tab. */
    @Query(
        """
        SELECT * FROM measurements
        ORDER BY createdAtEpochMillis DESC
        """
    )
    fun observeAll(): Flow<List<MeasurementEntity>>
}
