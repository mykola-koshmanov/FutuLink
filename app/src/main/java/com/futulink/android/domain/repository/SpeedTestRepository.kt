package com.futulink.android.domain.repository

import com.futulink.android.domain.model.SpeedTestUpdate
import kotlinx.coroutines.flow.Flow

interface SpeedTestRepository {

    /**
     * Runs one measurement. Collecting the flow starts the download, cancelling the
     * collection stops it. The flow fails with
     * [com.futulink.android.domain.model.SpeedTestException] when no result can be produced.
     */
    fun runSpeedTest(): Flow<SpeedTestUpdate>
}
