package kurou.kodriver.data.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
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
    val bufferFlow: Flow<ByteBuffer> = flow {
        try {
            while (true) {
                if (!reader.isOpen()) {
                    if (!reader.open()) {
                        delay(reconnectIntervalMs)
                        continue
                    }
                }
                reader.readBuffer()?.let { emit(it) }
                delay(pollingIntervalMs)
            }
        } finally {
            reader.close()
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        if (reader.isOpen()) return@withContext true
        val opened = reader.open()
        if (!opened) reader.close()
        opened
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        reader.close()
    }
}
