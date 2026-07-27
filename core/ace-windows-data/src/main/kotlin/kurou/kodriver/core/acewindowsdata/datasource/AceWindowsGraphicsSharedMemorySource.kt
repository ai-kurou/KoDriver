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
    internal val reader: SharedMemoryReader = WindowsSharedMemoryReader(
        segmentName = "Local\\acevo_pmf_graphics",
        sizeBytes = GRAPHICS_SEGMENT_SIZE_BYTES,
    ),
    scope: CoroutineScope,
) {
    private val pollingSource = SharedMemoryPollingSource(
        reader = reader,
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        scope = scope,
    )

    val bufferFlow: Flow<ByteBuffer> = pollingSource.bufferFlow

    private companion object {
        const val GRAPHICS_SEGMENT_SIZE_BYTES = 8_192
    }
}
