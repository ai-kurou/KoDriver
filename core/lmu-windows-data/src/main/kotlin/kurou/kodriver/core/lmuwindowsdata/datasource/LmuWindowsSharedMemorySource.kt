package kurou.kodriver.core.lmuwindowsdata.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryPollingSource
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import kurou.kodriver.core.windowssharedmemory.datasource.WindowsSharedMemoryReader
import java.nio.ByteBuffer
import kotlin.time.Clock

internal class LmuWindowsSharedMemorySource(
    pollingIntervalMs: Long = 16L,
    reconnectIntervalMs: Long = 1_000L,
    internal val reader: SharedMemoryReader =
        WindowsSharedMemoryReader(
            segmentName = "LMU_Data",
            sizeBytes = 324_820,
        ),
    private val currentTimeMs: () -> Long = { Clock.System.now().toEpochMilliseconds() },
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
            // 存在確認の前に自分のマッピングを解放することが必須。LMU が終了している場合、
            // 自分の MapViewOfFile がセクションを生かしている最後の参照になっているため。
            // 先に close してその参照を手放すことで、LMU が起動していなければ
            // OpenFileMappingA が失敗するようになる。bufferFlow はヒープへコピーした
            // バッファを emit し、ネイティブメモリ上の ByteBuffer を公開しないため、
            // 下流の呼び出し側に影響はない。
            reader.close()
            if (!reader.open()) return@withReaderLock false
            val buffer = reader.readBuffer() ?: return@withReaderLock false

            // OpenFileMappingA が成功しても、LMU の終了後に別のプロセス（Steam など）が
            // セクションを生かし続けている場合がある。これを検出するため mCurrentET が
            // 進んでいるかを確認し、ET_STALE_THRESHOLD_MS の間値が変化しなければ LMU は
            // 既に起動していないとみなす。
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
        // LMUObjectOut の scoring は +1632 から始まり、LMUScoringInfo の mCurrentET は +68。
        const val CURRENT_ET_OFFSET = 1632 + 68
        const val ET_STALE_THRESHOLD_MS = 3_000L
    }
}
