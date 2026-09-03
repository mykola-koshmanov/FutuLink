package com.futulink.android.presentation.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Fixed English pattern, e.g. "02 Sep 2026, 14:35". Localisation is out of scope, but the
 * device time zone is always respected so a stored UTC timestamp reads correctly.
 */
private val TIMESTAMP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)

fun formatTimestamp(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    TIMESTAMP_FORMATTER.format(Instant.ofEpochMilli(epochMillis).atZone(zoneId))

/** Rounding happens only here; stored and calculated values keep their full precision. */
fun formatMbps(value: Double): String = String.format(Locale.US, "%.2f", value)

fun formatSeconds(millis: Long): String = String.format(Locale.US, "%.1f", millis / 1000.0)
