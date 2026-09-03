package com.futulink.android.domain.model

/**
 * The test the application shows on the Test tab. The value comes from the remote
 * configuration on first launch and is cached afterwards.
 */
enum class TestMode {
    SPEED,
    PING;

    companion object {

        /** Any value the application does not recognise resolves to this mode. */
        val DEFAULT: TestMode = SPEED

        /**
         * Resolves a raw value (remote JSON payload or cached DataStore string) to a mode.
         * Surrounding whitespace is ignored and the comparison is case-insensitive, so
         * " Ping ", "PING" and "ping" all resolve to [PING]. Null, blank and unknown values
         * resolve to [DEFAULT].
         */
        fun fromRawValue(rawValue: String?): TestMode {
            val normalizedValue = rawValue?.trim().orEmpty()
            return entries.firstOrNull { mode -> mode.name.equals(normalizedValue, ignoreCase = true) }
                ?: DEFAULT
        }
    }
}
