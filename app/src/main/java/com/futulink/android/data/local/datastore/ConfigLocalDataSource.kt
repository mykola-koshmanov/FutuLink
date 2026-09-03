package com.futulink.android.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.futulink.android.domain.model.TestMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

private const val DATA_STORE_NAME = "futulink_config"

/** Single DataStore instance for the process, created lazily by the delegate. */
val Context.configDataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

/**
 * Caches the resolved test mode. The absence of the key is the only "not initialised yet"
 * signal the application needs, so there is no extra boolean flag.
 */
class ConfigLocalDataSource(private val dataStore: DataStore<Preferences>) {

    suspend fun getMode(): TestMode? {
        val preferences = dataStore.data
            // Only IO problems are recoverable here (missing or corrupted file). Anything
            // else is a programming error and must not be silently swallowed.
            .catch { throwable ->
                if (throwable is IOException) emit(emptyPreferences()) else throw throwable
            }
            .first()

        val storedValue = preferences[SELECTED_TEST_MODE_KEY] ?: return null
        // The key exists, so the app has initialised before. An unexpected stored string must
        // therefore resolve to the default mode instead of triggering a new network request.
        return TestMode.fromRawValue(storedValue)
    }

    /** Only ever called with an already normalised mode, after a successful config response. */
    suspend fun saveMode(mode: TestMode) {
        dataStore.edit { preferences -> preferences[SELECTED_TEST_MODE_KEY] = mode.name }
    }

    private companion object {
        val SELECTED_TEST_MODE_KEY = stringPreferencesKey("selected_test_mode")
    }
}
