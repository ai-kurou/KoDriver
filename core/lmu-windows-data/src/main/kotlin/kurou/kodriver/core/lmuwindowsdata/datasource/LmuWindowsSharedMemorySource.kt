package kurou.kodriver.core.lmuwindowsdata.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

internal class LmuWindowsSharedMemorySource(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
    internal val reader: SharedMemoryReader = WindowsSharedMemoryReader(
        segmentName = "LMU_Data",
        sizeBytes = 324_820,
    ),
    private val currentTimeMs: () -> Long = System::currentTimeMillis,
    scope: CoroutineScope,
) {
    private val readerMutex = Mutex()
    private var lastKnownEt: Double = Double.NaN
    private var lastEtChangeTimeMs: Long = 0L

    val bufferFlow: Flow<ByteBuffer> = flow {
        try {
            while (true) {
                val buffer = readerMutex.withLock {
                    if (!reader.isOpen() && !reader.open()) {
                        null
                    } else {
                        // Copy to a heap buffer so downstream never holds a native-backed
                        // reference. This lets isConnected() safely unmap/remap the reader
                        // without risking an access violation on Windows.
                        reader.readBuffer()?.let { native ->
                            val copy = ByteBuffer.allocate(native.limit()).order(native.order())
                            native.rewind()
                            copy.put(native)
                            copy.rewind()
                            copy
                        }
                    }
                }
                if (buffer == null) {
                    delay(reconnectIntervalMs)
                } else {
                    emit(buffer)
                    delay(pollingIntervalMs)
                }
            }
        } finally {
            readerMutex.withLock { reader.close() }
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        readerMutex.withLock {
            // Releasing our mapping before probing is essential: if LMU has exited,
            // our MapViewOfFile is the last reference keeping the section alive.
            // Closing first drops that reference, so OpenFileMappingA will fail when
            // LMU is not running. Downstream callers are safe because bufferFlow emits
            // heap-copied buffers and never exposes the native-backed ByteBuffer.
            reader.close()
            if (!reader.open()) return@withLock false
            val buffer = reader.readBuffer() ?: return@withLock false

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
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        readerMutex.withLock { reader.close() }
    }

    private companion object {
        // LMUObjectOut: scoring starts at 1632; LMUScoringInfo: mCurrentET at +68
        const val CURRENT_ET_OFFSET = 1632 + 68
        const val ET_STALE_THRESHOLD_MS = 3_000L
    }
}
