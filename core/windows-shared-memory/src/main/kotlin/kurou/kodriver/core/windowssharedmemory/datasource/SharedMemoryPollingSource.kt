package kurou.kodriver.core.windowssharedmemory.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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

/**
 * [SharedMemoryReader] を一定間隔でポーリングし、読み取ったバッファを [bufferFlow] として配信する。
 * open/close の排他制御用に単一の [Mutex] を保持しており、[withReaderLock] を通じて
 * 呼び出し元固有の追加処理（接続判定など）を同じ排他制御の下で安全に行える。
 */
class SharedMemoryPollingSource(
    private val reader: SharedMemoryReader,
    private val pollingIntervalMs: Long,
    private val reconnectIntervalMs: Long,
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
                        // reference, allowing withReaderLock callers to safely unmap/remap
                        // the reader without risking an access violation on Windows.
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
            withContext(NonCancellable) {
                readerMutex.withLock { reader.close() }
            }
        }
    }
        .flowOn(Dispatchers.IO)
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0)

    /**
     * [bufferFlow] のポーリングループと同じ [Mutex] の下で [reader] を操作する。
     * 接続判定・切断など、reader の open/close を伴う追加処理をポーリングループと
     * 競合させずに実行するために使用する。
     */
    suspend fun <T> withReaderLock(block: suspend (SharedMemoryReader) -> T): T =
        withContext(Dispatchers.IO) {
            readerMutex.withLock { block(reader) }
        }
}
