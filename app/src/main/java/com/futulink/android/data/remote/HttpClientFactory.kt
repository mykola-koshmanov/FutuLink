package com.futulink.android.data.remote

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val SOCKET_TIMEOUT_MILLIS = 15_000L

/**
 * Shared client configuration. Production applies it to the OkHttp engine; the unit tests apply
 * it to Ktor's MockEngine (config parsing) and to the OkHttp engine against a loopback server
 * (streaming cancellation), so every path parses and times out by the same rules.
 */
fun HttpClientConfig<*>.configureFutuLinkClient() {
    // Any non-2xx response throws, which is exactly what "config loading failed" means here.
    expectSuccess = true

    install(ContentNegotiation) {
        val jsonFormat = Json { ignoreUnknownKeys = true }
        json(jsonFormat)
        // GitHub raw serves the config file as text/plain, so the JSON converter has to be
        // registered for that content type as well or the response cannot be converted.
        json(jsonFormat, ContentType.Text.Plain)
    }

    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }
}
