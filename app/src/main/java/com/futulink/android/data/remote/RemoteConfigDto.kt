package com.futulink.android.data.remote

import kotlinx.serialization.Serializable

/**
 * Wire format of the remote configuration file.
 *
 * [mode] is nullable with a default so a payload that omits the property still parses; the
 * domain layer then applies the default mode. Unknown properties are ignored by the Json
 * configuration, so the file can grow without breaking older builds.
 */
@Serializable
data class RemoteConfigDto(
    val mode: String? = null,
)
