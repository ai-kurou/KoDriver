package kurou.kodriver.data.datasource

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

internal class SharedLmuMemorySource(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
    internal val reader: MemoryReader = SharedMemoryReader(
        segmentName = "LMU_Data",
        sizeBytes = 324_820,
    ),
    private val probeReaderFactory: () -> MemoryReader = {
        SharedMemoryReader(segmentName = "LMU_Data", sizeBytes = 324_820)
    },
    scope: CoroutineScope,
) {
    private val readerMutex = Mutex()

    val bufferFlow: Flow<ByteBuffer> = flow {
        try {
            while (true) {
                val buffer = readerMutex.withLock {
                    if (!reader.isOpen() && !reader.open()) {
                        null
                    } else {
                        reader.readBuffer()
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
        // Use a separate probe reader so bufferFlow's mapped ByteBuffer is never unmapped.
        // Closing the shared reader while downstream code holds a reference to its native-
        // backed ByteBuffer would cause an access violation on Windows.
        val probe = probeReaderFactory()
        val connected = probe.open() && probe.readBuffer() != null
        probe.close()
        connected
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        readerMutex.withLock { reader.close() }
    }
}
