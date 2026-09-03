package com.futulink.android.data.repository

import com.futulink.android.data.local.datastore.ConfigLocalDataSource
import com.futulink.android.data.remote.ConfigRemoteDataSource
import com.futulink.android.domain.model.ConfigLoadException
import com.futulink.android.domain.model.TestMode
import com.futulink.android.domain.repository.ConfigRepository
import kotlin.coroutines.cancellation.CancellationException

class ConfigRepositoryImpl(
    private val localDataSource: ConfigLocalDataSource,
    private val remoteDataSource: ConfigRemoteDataSource,
) : ConfigRepository {

    override suspend fun getCachedMode(): TestMode? = localDataSource.getMode()

    override suspend fun fetchAndCacheMode(): TestMode {
        val remoteConfig = try {
            remoteDataSource.fetchConfig()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Ktor exception types stop here: the rest of the app only knows this domain type.
            // Nothing is written to DataStore, so a failed first launch stays uninitialised.
            throw ConfigLoadException(exception)
        }

        val mode = TestMode.fromRawValue(remoteConfig.mode)
        localDataSource.saveMode(mode)
        return mode
    }
}
