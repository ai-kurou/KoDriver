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
            reader.open() && reader.readBuffer() != null
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        readerMutex.withLock { reader.close() }
    }
}
