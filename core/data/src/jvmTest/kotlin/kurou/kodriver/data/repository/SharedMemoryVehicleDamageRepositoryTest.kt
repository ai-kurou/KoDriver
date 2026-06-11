@file:Suppress("FunctionNaming")

package kurou.kodriver.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.data.datasource.MemoryReader
import kurou.kodriver.data.datasource.SharedLmuMemorySource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedMemoryVehicleDamageRepositoryTest {

    private fun makeSource(
        reader: MemoryReader,
        pollingIntervalMs: Long = 1L,
        reconnectIntervalMs: Long = 1L,
    ) = SharedLmuMemorySource(
        pollingIntervalMs = pollingIntervalMs,
        reconnectIntervalMs = reconnectIntervalMs,
        reader = reader,
        scope = CoroutineScope(SupervisorJob()),
    )

    @Test
    fun `共有メモリから overheating と partDetached と lastImpactMagnitude を読み取る`() = runBlocking {
        val reader = FakeDamageMemoryReader(
            buildDamageBuffer(
                DamageBufferConfig(
                    overheating = true,
                    partDetached = true,
                    lastImpactMagnitude = 42.5,
                ),
            ),
        )
        val repo = SharedMemoryVehicleDamageRepository(source = makeSource(reader))

        val result = repo.vehicleDamageStream().first()

        assertTrue(result.overheating)
        assertTrue(result.partDetached)
        assertEquals(42.5, result.lastImpactMagnitude)
    }

    @Test
    fun `overheating と partDetached が false のとき false を返す`() = runBlocking {
        val reader = FakeDamageMemoryReader(
            buildDamageBuffer(DamageBufferConfig(overheating = false, partDetached = false)),
        )
        val repo = SharedMemoryVehicleDamageRepository(source = makeSource(reader))

        val result = repo.vehicleDamageStream().first()

        assertFalse(result.overheating)
        assertFalse(result.partDetached)
    }

    @Test
    fun `activeVehicles が 0 のとき emit しない`() = runBlocking {
        val reader = FakeDamageMemoryReader(
            buildDamageBuffer(DamageBufferConfig(activeVehicles = 0)),
        )
        val repo = SharedMemoryVehicleDamageRepository(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.vehicleDamageStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    @Test
    fun `reader が open できない間は emit しない`() = runBlocking {
        val reader = FakeDamageMemoryReader(
            buffer = buildDamageBuffer(),
            openResult = false,
        )
        val repo = SharedMemoryVehicleDamageRepository(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.vehicleDamageStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    private fun buildDamageBuffer(config: DamageBufferConfig = DamageBufferConfig()): ByteBuffer {
        val telemetryBase = 128_464
        val telemInfo = 4
        val vehicleStride = 1_888
        val buffer = ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(telemetryBase + 0, config.activeVehicles.toByte())
        buffer.put(telemetryBase + 1, config.playerIdx.toByte())

        val vehicleBase = telemetryBase + telemInfo + config.playerIdx * vehicleStride
        buffer.put(vehicleBase + 541, if (config.overheating) 1.toByte() else 0.toByte())
        buffer.put(vehicleBase + 542, if (config.partDetached) 1.toByte() else 0.toByte())
        buffer.putDouble(vehicleBase + 560, config.lastImpactMagnitude)

        return buffer
    }

    private data class DamageBufferConfig(
        val activeVehicles: Int = 1,
        val playerIdx: Int = 0,
        val overheating: Boolean = false,
        val partDetached: Boolean = false,
        val lastImpactMagnitude: Double = 0.0,
    )
}

private class FakeDamageMemoryReader(
    private val buffer: ByteBuffer,
    private val openResult: Boolean = true,
) : MemoryReader {

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
