package kurou.kodriver.core.lmuwindowsdata.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryPollingSource
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import kurou.kodriver.core.windowssharedmemory.datasource.WindowsSharedMemoryReader
import java.nio.ByteBuffer

internal class LmuWindowsSharedMemorySource(
    pollingIntervalMs: Long = 16L,
    reconnectIntervalMs: Long = 1_000L,
    internal val reader: SharedMemoryReader =
        WindowsSharedMemoryReader(
            segmentName = "LMU_Data",
            sizeBytes = 324_820,
        ),
    private val currentTimeMs: () -> Long = System::currentTimeMillis,
    scope: CoroutineScope,
) {
    private val pollingSource =
        SharedMemoryPollingSource(
            reader = reader,
            pollingIntervalMs = pollingIntervalMs,
            reconnectIntervalMs = reconnectIntervalMs,
            scope = scope,
        )
    private var lastKnownEt: Double = Double.NaN
    private var lastEtChangeTimeMs: Long = 0L

    val bufferFlow: Flow<ByteBuffer> = pollingSource.bufferFlow

    suspend fun isConnected(): Boolean =
        pollingSource.withReaderLock { reader ->
            // Releasing our mapping before probing is essential: if LMU has exited,
            // our MapViewOfFile is the last reference keeping the section alive.
            // Closing first drops that reference, so OpenFileMappingA will fail when
            // LMU is not running. Downstream callers are safe because bufferFlow emits
            // heap-copied buffers and never exposes the native-backed ByteBuffer.
            reader.close()
            if (!reader.open()) return@withReaderLock false
            val buffer = reader.readBuffer() ?: return@withReaderLock false

            // Even if OpenFileMappingA succeeds, another process (e.g. Steam) may hold
            // the section alive after LMU has exited. Guard against this by checking
            // whether mCurrentET is still advancing. If the value has not changed for
            // ET_STALE_THRESHOLD_MS, assume LMU is no longer running.
            val currentEt = buffer.getDouble(CURRENT_ET_OFFSET)
            val nowMs = currentTimeMs()
            if (currentEt != lastKnownEt) {
                lastKnownEt = currentEt
                lastEtChangeTimeMs = nowMs
            }
            nowMs - lastEtChangeTimeMs < ET_STALE_THRESHOLD_MS
        }

    suspend fun disconnect() = pollingSource.withReaderLock { reader -> reader.close() }

    private companion object {
        // LMUObjectOut: scoring starts at 1632; LMUScoringInfo: mCurrentET at +68
        const val CURRENT_ET_OFFSET = 1632 + 68
        const val ET_STALE_THRESHOLD_MS = 3_000L
    }
}
