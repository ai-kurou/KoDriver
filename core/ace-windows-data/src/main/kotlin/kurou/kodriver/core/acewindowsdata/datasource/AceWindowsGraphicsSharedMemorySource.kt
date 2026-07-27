package kurou.kodriver.core.acewindowsdata.datasource

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
import java.nio.ByteBuffer

/**
 * Assetto Corsa EVO の Graphics 共有メモリ（`Local\acevo_pmf_graphics`）をポーリングし、
 * ByteBuffer として配信する。実データサイズは早期アクセス版で約6〜8KBのため、
 * 余裕を持たせた [GRAPHICS_SEGMENT_SIZE_BYTES] でマッピングする。
 */
internal class AceWindowsGraphicsSharedMemorySource(
    private val pollingIntervalMs: Long = 16L,
    private val reconnectIntervalMs: Long = 1_000L,
    internal val reader: SharedMemoryReader = WindowsSharedMemoryReader(
        segmentName = "Local\\acevo_pmf_graphics",
        sizeBytes = GRAPHICS_SEGMENT_SIZE_BYTES,
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
                        // reference, mirroring LmuWindowsSharedMemorySource's approach.
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

    private companion object {
        const val GRAPHICS_SEGMENT_SIZE_BYTES = 8_192
    }
}
