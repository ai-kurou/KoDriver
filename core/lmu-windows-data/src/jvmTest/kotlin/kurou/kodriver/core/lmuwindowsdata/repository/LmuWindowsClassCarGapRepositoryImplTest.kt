package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsClassCarGapRepositoryImplTest {
    private fun makeSource(
        reader: SharedMemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = LmuWindowsSharedMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `Scoringの自車スロットから同クラスのギャップを配信する`() =
        runBlocking {
            val reader = FakeClassCarGapMemoryReader(buildClassCarGapBuffer())
            val repo = LmuWindowsClassCarGapRepositoryImpl(makeSource(reader))

            val result = repo.classCarGapStream().first()

            assertEquals(5.0, result.gapAheadSeconds)
            assertEquals(95.0, result.gapBehindSeconds)
            assertEquals(2, result.classPosition)
            assertEquals(2, result.classVehicleCount)
        }

    @Test
    fun `自車がなければ配信しない`() =
        runBlocking {
            val buffer = buildClassCarGapBuffer()
            buffer.put(2192 + 584 + 196, 0)
            val repo = LmuWindowsClassCarGapRepositoryImpl(makeSource(FakeClassCarGapMemoryReader(buffer)))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.classCarGapStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    private fun buildClassCarGapBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(1632 + 104, 2)
        val playerBase = 2192 + 584
        buffer.put(playerBase + 196, 1)
        buffer.put(playerBase + 199, 2)
        buffer.put(2192 + 199, 1)
        buffer.putDouble(playerBase + 464, 20.0)
        buffer.putDouble(playerBase + 472, 100.0)
        buffer.putDouble(2192 + 464, 25.0)
        return buffer
    }
}

private class FakeClassCarGapMemoryReader(
    private val buffer: ByteBuffer,
    private val openResult: Boolean = true,
) : SharedMemoryReader {
    private var opened = openResult

    override fun open(): Boolean {
        opened = openResult
        return openResult
    }

    override fun readBuffer(): ByteBuffer? {
        if (!opened) return null
        return ByteBuffer.wrap(buffer.array().copyOf()).order(ByteOrder.LITTLE_ENDIAN)
    }

    override fun isOpen(): Boolean = opened

    override fun close() {
        opened = false
    }
}
