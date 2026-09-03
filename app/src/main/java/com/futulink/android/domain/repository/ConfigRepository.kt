package com.futulink.android.domain.repository

import com.futulink.android.domain.model.TestMode

interface ConfigRepository {

    /** Cached mode, or null when the application has never initialised successfully. */
    suspend fun getCachedMode(): TestMode?

    /**
     * Performs the real config request, caches the resolved mode and returns it.
     * Throws [com.futulink.android.domain.model.ConfigLoadException] when the request fails;
     * nothing is cached in that case.
     */
    suspend fun fetchAndCacheMode(): TestMode
}
