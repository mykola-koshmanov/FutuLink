package com.futulink.android.data.repository

import android.os.SystemClock
import com.futulink.android.domain.model.SpeedCalculator
import com.futulink.android.domain.model.SpeedTestConfig.SAMPLE_INTERVAL_MILLIS
import com.futulink.android.domain.model.SpeedTestConfig.TEST_DURATION_MILLIS
import com.futulink.android.domain.model.SpeedTestException
import com.futulink.android.domain.model.SpeedTestFailureReason
import com.futulink.android.domain.model.SpeedTestResult
import com.futulink.android.domain.model.SpeedTestUpdate
import com.futulink.android.domain.repository.SpeedTestRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/** Reused for every read, so a full test allocates one buffer instead of one per chunk. */
private const val BUFFER_SIZE_BYTES = 64 * 1024

/**
 * Longer than the default client timeout: the lifetime of this request is controlled by the
 * measurement scope, which cancels it at the 10 s deadline.
 */
private const val DOWNLOAD_REQUEST_TIMEOUT_MILLIS = 60_000L

private const val NANOS_PER_MILLI = 1_000_000L

class SpeedTestRepositoryImpl(
    private val httpClient: HttpClient,
    private val downloadUrl: String,
) : SpeedTestRepository {

    /**
     * One child coroutine streams the download and updates a shared counter; the builder body
     * samples that counter every [SAMPLE_INTERVAL_MILLIS] and emits live values. Both are
     * children of the same scope, so cancelling the collector stops the traffic as well.
     *
     * External cancellation (Stop, leaving the screen, cleared ViewModel) simply propagates out
     * of both coroutines: nothing catches it, so no result is emitted and nothing is saved.
     */
    override fun runSpeedTest(): Flow<SpeedTestUpdate> = channelFlow {
        val receivedBytes = AtomicLong(0L)
        // Monotonic clock: wall-clock time can jump (NTP sync, manual change) and would
        // silently corrupt every duration this measurement is based on.
        val startNanos = SystemClock.elapsedRealtimeNanos()

        val downloadJob = launch(Dispatchers.IO) { download(receivedBytes) }

        val samples = mutableListOf<Double>()
        var previousBytes = 0L
        var previousSampleNanos = startNanos

        // Filled in when the deadline is reached; these two values are the measured window.
        var measurementEndNanos = startNanos
        var bytesAtDeadline = 0L

        while (true) {
            delay(SAMPLE_INTERVAL_MILLIS)

            // Clock and counter are read together, so the bytes and the duration below always
            // describe exactly the same instant.
            val sampleNanos = SystemClock.elapsedRealtimeNanos()
            val bytesSoFar = receivedBytes.get()

            // The measured interval length is used instead of the nominal 500 ms, because the
            // dispatcher can wake this coroutine late and that would overstate the speed.
            val currentMbps = SpeedCalculator.toMbps(
                bytes = bytesSoFar - previousBytes,
                durationNanos = sampleNanos - previousSampleNanos,
            )
            samples += currentMbps
            previousBytes = bytesSoFar
            previousSampleNanos = sampleNanos

            val elapsedMillis = (sampleNanos - startNanos) / NANOS_PER_MILLI
            send(
                SpeedTestUpdate.Progress(
                    currentMbps = currentMbps,
                    elapsedMillis = elapsedMillis.coerceAtMost(TEST_DURATION_MILLIS),
                    progress = (elapsedMillis.toFloat() / TEST_DURATION_MILLIS).coerceIn(0f, 1f),
                )
            )

            if (elapsedMillis >= TEST_DURATION_MILLIS) {
                measurementEndNanos = sampleNanos
                bytesAtDeadline = bytesSoFar
                break
            }
        }

        // The window is frozen above, so cancelling the request and closing the response happens
        // outside the measured duration and bytes arriving during that cleanup are not counted.
        // Reaching the deadline is the measurement's own completion signal, not user cancellation.
        downloadJob.cancel()
        downloadJob.join()

        if (bytesAtDeadline == 0L) {
            throw SpeedTestException(SpeedTestFailureReason.NO_DATA)
        }

        // Emitted only after the download coroutine finished, so no traffic outlives the result.
        send(
            SpeedTestUpdate.Completed(
                SpeedTestResult(
                    averageMbps = SpeedCalculator.toMbps(
                        bytes = bytesAtDeadline,
                        durationNanos = measurementEndNanos - startNanos,
                    ),
                    peakMbps = samples.maxOrNull() ?: 0.0,
                )
            )
        )
    }

    /**
     * Streams the response in 64 KB chunks and counts the bytes. The body is never turned into a
     * ByteArray or String, so the 100 MiB download never sits in memory.
     */
    private suspend fun download(receivedBytes: AtomicLong) {
        val buffer = ByteArray(BUFFER_SIZE_BYTES)
        try {
            // The endpoint serves a finite body. A fast connection can consume all of it before
            // the window ends, so the next request is simply started and the counter keeps going.
            while (currentCoroutineContext().isActive) {
                httpClient.prepareGet(downloadUrl) {
                    // Only the response body is counted. Asking for identity encoding keeps the
                    // server from compressing it, which would otherwise understate the bytes moved.
                    header(HttpHeaders.AcceptEncoding, "identity")
                    header(HttpHeaders.CacheControl, "no-cache")
                    timeout { requestTimeoutMillis = DOWNLOAD_REQUEST_TIMEOUT_MILLIS }
                }.execute { response ->
                    // execute { } closes the response and releases its connection when the block
                    // returns, throws, or is cancelled.
                    val channel = response.bodyAsChannel()
                    while (currentCoroutineContext().isActive) {
                        val readBytes = channel.readAvailable(buffer, 0, buffer.size)
                        if (readBytes < 0) break // end of this response body
                        receivedBytes.addAndGet(readBytes.toLong())
                    }
                }
            }
        } catch (exception: CancellationException) {
            // Deadline reached, Stop pressed, screen left or ViewModel cleared: never an error.
            throw exception
        } catch (exception: Exception) {
            // Failing here cancels the whole measurement scope, so no partial result is emitted.
            throw SpeedTestException(SpeedTestFailureReason.NETWORK, exception)
        }
    }
}
