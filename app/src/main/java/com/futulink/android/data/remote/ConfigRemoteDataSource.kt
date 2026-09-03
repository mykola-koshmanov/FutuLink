package com.futulink.android.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.serialization.ContentConvertException
import kotlin.coroutines.cancellation.CancellationException

class ConfigRemoteDataSource(
    private val httpClient: HttpClient,
    private val configUrl: String,
) {

    /**
     * Performs the real config request.
     *
     * Transport problems (offline, DNS, timeout, non-success status) are thrown so the
     * repository can report a loading failure. A successful response whose body is malformed
     * or has an unexpected shape is *not* a transport problem: it returns an empty DTO and the
     * domain layer applies the default mode.
     */
    suspend fun fetchConfig(): RemoteConfigDto {
        val response = httpClient.get(configUrl)
        return try {
            response.body<RemoteConfigDto>()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ContentConvertException) {
            RemoteConfigDto()
        } catch (exception: NoTransformationFoundException) {
            RemoteConfigDto()
        }
    }
}
