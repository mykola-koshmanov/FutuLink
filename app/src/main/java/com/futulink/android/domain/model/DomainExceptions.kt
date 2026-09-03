package com.futulink.android.domain.model

/**
 * The remote configuration could not be loaded: timeout, DNS failure, no connectivity or a
 * non-success HTTP status. The original cause is kept for logging but never shown to users.
 */
class ConfigLoadException(cause: Throwable) : Exception(cause)

/** Why a speed test could not produce a result. */
enum class SpeedTestFailureReason {
    /** The download could not be started or was interrupted by a network problem. */
    NETWORK,

    /** The request succeeded but not a single byte arrived, so there is nothing to report. */
    NO_DATA,
}

/** A speed test that failed. Cancellation is never reported through this exception. */
class SpeedTestException(
    val reason: SpeedTestFailureReason,
    cause: Throwable? = null,
) : Exception(cause)
