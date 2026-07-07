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
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreCarcassTemperatureRepositoryImplTest {

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
    fun `共有メモリから4輪のカーカス温度を読み取る`() = runBlocking {
        val reader = FakeTyreCarcassTemperatureMemoryReader(
            buildTyreCarcassTemperatureBuffer(
                TyreCarcassTemperatureBufferConfig(
                    temperatures = mapOf(
                        WheelIndex.FRONT_LEFT to 350.0,
                        WheelIndex.FRONT_RIGHT to 351.0,
                        WheelIndex.REAR_LEFT to 352.0,
                        WheelIndex.REAR_RIGHT to 353.0,
                    ),
                ),
            ),
        )
        val repo = LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(reader))

        val result = repo.tyreCarcassTemperatureStream().first()

        assertEquals(350.0 - 273.15, result.wheels[WheelIndex.FRONT_LEFT]!!, 1e-9)
        assertEquals(351.0 - 273.15, result.wheels[WheelIndex.FRONT_RIGHT]!!, 1e-9)
        assertEquals(352.0 - 273.15, result.wheels[WheelIndex.REAR_LEFT]!!, 1e-9)
        assertEquals(353.0 - 273.15, result.wheels[WheelIndex.REAR_RIGHT]!!, 1e-9)
    }

    @Test
    fun `playerIndexに応じた車両スロットからカーカス温度を読み取る`() = runBlocking {
        val reader = FakeTyreCarcassTemperatureMemoryReader(
            buildTyreCarcassTemperatureBuffer(
                TyreCarcassTemperatureBufferConfig(
                    activeVehicles = 2,
                    playerIdx = 1,
                    temperatures = mapOf(WheelIndex.FRONT_LEFT to 365.0),
                ),
            ),
        )
        val repo = LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(reader))

        val result = repo.tyreCarcassTemperatureStream().first()

        assertEquals(365.0 - 273.15, result.wheels[WheelIndex.FRONT_LEFT]!!, 1e-9)
    }

    @Test
    fun `activeVehicles が 0 のとき emit しない`() = runBlocking {
        val reader = FakeTyreCarcassTemperatureMemoryReader(
            buildTyreCarcassTemperatureBuffer(TyreCarcassTemperatureBufferConfig(activeVehicles = 0)),
        )
        val repo = LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.tyreCarcassTemperatureStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    @Test
    fun `playerIdxがactiveVehicles以上のとき emit しない`() = runBlocking {
        val reader = FakeTyreCarcassTemperatureMemoryReader(
            buildTyreCarcassTemperatureBuffer(
                TyreCarcassTemperatureBufferConfig(activeVehicles = 1, playerIdx = 1),
            ),
        )
        val repo = LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.tyreCarcassTemperatureStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    @Test
    fun `reader が open できない間は emit しない`() = runBlocking {
        val reader = FakeTyreCarcassTemperatureMemoryReader(
            buffer = buildTyreCarcassTemperatureBuffer(),
            openResult = false,
        )
        val repo = LmuWindowsTyreCarcassTemperatureRepositoryImpl(source = makeSource(reader))
        val emitCount = AtomicInteger(0)

        val job = launch { repo.tyreCarcassTemperatureStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
    }

    private fun buildTyreCarcassTemperatureBuffer(
        config: TyreCarcassTemperatureBufferConfig = TyreCarcassTemperatureBufferConfig(),
    ): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, config.activeVehicles.toByte())
        buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, config.playerIdx.toByte())

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + config.playerIdx * VEHICLE_STRIDE
        WheelIndex.entries.forEach { wheel ->
            val wheelBase = vehicleBase + OFF_WHEELS + wheel.ordinal * WHEEL_STRIDE
            buffer.putDouble(wheelBase + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE, config.temperatures[wheel] ?: 0.0)
        }

        return buffer
    }

    private data class TyreCarcassTemperatureBufferConfig(
        val activeVehicles: Int = 1,
        val playerIdx: Int = 0,
        val temperatures: Map<WheelIndex, Double> = emptyMap(),
    )

    private companion object {
        const val TELEMETRY_BASE = 128_464
        const val OFF_ACTIVE_VEHICLES = 0
        const val OFF_PLAYER_VEHICLE_IDX = 1
        const val OFF_TELEM_INFO = 4
        const val VEHICLE_STRIDE = 1_888
        const val OFF_WHEELS = 848
        const val WHEEL_STRIDE = 260
        const val OFF_WHEEL_TIRE_CARCASS_TEMPERATURE = 204
        const val BUFFER_SIZE = 135_000
    }
}

private class FakeTyreCarcassTemperatureMemoryReader(
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
