@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.lmuwindowsdata.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVirtualEnergyRepositoryImplTest {

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
    fun `共有メモリからバーチャルエナジー残量割合を読み取る`() = runBlocking {
        val reader = FakeVirtualEnergyMemoryReader(
            buildVirtualEnergyBuffer(VirtualEnergyBufferConfig(remainingRatio = 0.75f)),
        )
        val repo = LmuWindowsVirtualEnergyRepositoryImpl(source = makeSource(reader))

        val result = repo.virtualEnergyStream().first()

        assertEquals(0.75, result.remainingRatio, 1e-6)
    }

    @Test
    fun `playerIndexに応じた車両スロットからバーチャルエナジー残量割合を読み取る`() = runBlocking {
        val reader = FakeVirtualEnergyMemoryReader(
            buildVirtualEnergyBuffer(
                VirtualEnergyBufferConfig(activeVehicles = 2, playerIdx = 1, remainingRatio = 0.3f),
            ),
        )
        val repo = LmuWindowsVirtualEnergyRepositoryImpl(source = makeSource(reader))

        val result = repo.virtualEnergyStream().first()

        assertEquals(0.3, result.remainingRatio, 1e-6)
    }

    @Test
    fun `activeVehicles が 0 のとき emit しない`() = runBlocking {
        val reader = FakeVirtualEnergyMemoryReader(
            buildVirtualEnergyBuffer(VirtualEnergyBufferConfig(activeVehicles = 0)),
        )
        val repo = LmuWindowsVirtualEnergyRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.virtualEnergyStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    @Test
    fun `playerIdxがactiveVehicles以上のとき emit しない`() = runBlocking {
        val reader = FakeVirtualEnergyMemoryReader(
            buildVirtualEnergyBuffer(VirtualEnergyBufferConfig(activeVehicles = 1, playerIdx = 1)),
        )
        val repo = LmuWindowsVirtualEnergyRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.virtualEnergyStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    @Test
    fun `reader が open できない間は emit しない`() = runBlocking {
        val reader = FakeVirtualEnergyMemoryReader(
            buffer = buildVirtualEnergyBuffer(),
            openResult = false,
        )
        val repo = LmuWindowsVirtualEnergyRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.virtualEnergyStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    private fun buildVirtualEnergyBuffer(
        config: VirtualEnergyBufferConfig = VirtualEnergyBufferConfig(),
    ): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, config.activeVehicles.toByte())
        buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, config.playerIdx.toByte())

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + config.playerIdx * VEHICLE_STRIDE
        buffer.putFloat(vehicleBase + OFF_VIRTUAL_ENERGY, config.remainingRatio)

        return buffer
    }

    private data class VirtualEnergyBufferConfig(
        val activeVehicles: Int = 1,
        val playerIdx: Int = 0,
        val remainingRatio: Float = 0.0f,
    )

    private companion object {
        const val TELEMETRY_BASE = 128_464
        const val OFF_ACTIVE_VEHICLES = 0
        const val OFF_PLAYER_VEHICLE_IDX = 1
        const val OFF_TELEM_INFO = 4
        const val VEHICLE_STRIDE = 1_888
        const val OFF_VIRTUAL_ENERGY = 776
        const val BUFFER_SIZE = 135_000
    }
}

private class FakeVirtualEnergyMemoryReader(
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
