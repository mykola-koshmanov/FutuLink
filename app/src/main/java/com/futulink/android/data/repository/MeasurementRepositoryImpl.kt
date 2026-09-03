package com.futulink.android.data.repository

import com.futulink.android.data.local.room.MeasurementDao
import com.futulink.android.data.mapper.toDomain
import com.futulink.android.data.mapper.toEntity
import com.futulink.android.domain.model.Measurement
import com.futulink.android.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepositoryImpl(private val measurementDao: MeasurementDao) : MeasurementRepository {

    override fun observeMeasurements(): Flow<List<Measurement>> =
        measurementDao.observeAll().map { entities -> entities.map { entity -> entity.toDomain() } }

    override suspend fun saveMeasurement(measurement: Measurement) {
        measurementDao.insert(measurement.toEntity())
    }
}
