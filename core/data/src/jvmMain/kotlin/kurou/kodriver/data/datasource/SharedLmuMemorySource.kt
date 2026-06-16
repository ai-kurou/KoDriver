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
        readerMutex.withLock {
            // Always close first to release the handle. If LMU has exited and no other
            // process holds the mapping, it gets destroyed so open() will correctly fail.
            reader.close()
            if (!reader.open()) return@withLock false
            reader.readBuffer() != null
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        readerMutex.withLock { reader.close() }
    }
}
