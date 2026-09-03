package com.futulink.android.domain.model

/**
 * Throughput math for the speed test. Decimal megabits are used (1 Mbit = 1,000,000 bits),
 * which is the convention internet providers and speed test services use.
 */
object SpeedCalculator {

    private const val BITS_PER_BYTE = 8
    private const val BITS_PER_MEGABIT = 1_000_000
    private const val NANOS_PER_SECOND = 1_000_000_000.0

    /** Mbps = bytes * 8 / seconds / 1,000,000. Returns 0.0 for empty or zero-length samples. */
    fun toMbps(bytes: Long, durationNanos: Long): Double {
        if (bytes <= 0L || durationNanos <= 0L) return 0.0
        val seconds = durationNanos / NANOS_PER_SECOND
        return bytes * BITS_PER_BYTE / seconds / BITS_PER_MEGABIT
    }
}
