package com.futulink.android.domain.model

/**
 * Parameters of one speed measurement. They live in the domain because both the data layer that
 * runs the measurement and the UI that labels it ("0.0 s of 10 s") have to agree on them.
 */
object SpeedTestConfig {

    /** Length of the measurement window. */
    const val TEST_DURATION_MILLIS = 10_000L

    /** How often the byte counter is sampled to produce a live speed value. */
    const val SAMPLE_INTERVAL_MILLIS = 500L
}
