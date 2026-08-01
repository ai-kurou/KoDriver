package kurou.kodriver.core.acewindowsdata.datasource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryPollingSource
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import kurou.kodriver.core.windowssharedmemory.datasource.WindowsSharedMemoryReader
import java.nio.ByteBuffer

/**
 * Assetto Corsa EVO の Graphics 共有メモリ（`Local\acevo_pmf_graphics`）をポーリングし、
 * ByteBuffer として配信する。実データサイズは早期アクセス版で約6〜8KBのため、
 * 余裕を持たせた [GRAPHICS_SEGMENT_SIZE_BYTES] でマッピングする。
 */
internal class AceWindowsGraphicsSharedMemorySource(
    pollingIntervalMs: Long = 16L,
    reconnectIntervalMs: Long = 1_000L,
    internal val reader: SharedMemoryReader =
        WindowsSharedMemoryReader(
        segmentName = "Local\\acevo_pmf_graphics",
        sizeBytes = GRAPHICS_SEGMENT_SIZE_BYTES,
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
    private var lastKnownPacketId: Int? = null
    private var lastPacketIdChangeTimeMs: Long = 0L

    val bufferFlow: Flow<ByteBuffer> = pollingSource.bufferFlow

    suspend fun isConnected(): Boolean =
        pollingSource.withReaderLock { reader ->
        // LmuWindowsSharedMemorySource と同様、他プロセスの参照によってセクションが
        // 生き残っているケースに対応するため、まず close してから開き直す。
        reader.close()
        if (!reader.open()) return@withReaderLock false
        val buffer = reader.readBuffer() ?: return@withReaderLock false

        // packetId（+0）はフレームごとに増加するカウンタ。ACE が終了・停止した場合は
        // 値が変化しなくなるため、PACKET_ID_STALE_THRESHOLD_MS 以上変化がなければ
        // 実質的に切断されたとみなす。
        val currentPacketId = buffer.getInt(PACKET_ID_OFFSET)
        val nowMs = currentTimeMs()
        if (currentPacketId != lastKnownPacketId) {
            lastKnownPacketId = currentPacketId
            lastPacketIdChangeTimeMs = nowMs
        }
        nowMs - lastPacketIdChangeTimeMs < PACKET_ID_STALE_THRESHOLD_MS
    }

    private companion object {
        const val GRAPHICS_SEGMENT_SIZE_BYTES = 8_192
        const val PACKET_ID_OFFSET = 0
        const val PACKET_ID_STALE_THRESHOLD_MS = 3_000L
    }
}
