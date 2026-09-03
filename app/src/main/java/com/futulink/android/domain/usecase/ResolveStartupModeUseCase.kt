package com.futulink.android.domain.usecase

import com.futulink.android.domain.model.TestMode
import com.futulink.android.domain.repository.ConfigRepository

/**
 * Decides which test the application shows. The cache is asked first, so the remote
 * request only happens on a launch that follows a never-initialised installation.
 */
class ResolveStartupModeUseCase(private val configRepository: ConfigRepository) {

    suspend operator fun invoke(): TestMode =
        configRepository.getCachedMode() ?: configRepository.fetchAndCacheMode()
}
