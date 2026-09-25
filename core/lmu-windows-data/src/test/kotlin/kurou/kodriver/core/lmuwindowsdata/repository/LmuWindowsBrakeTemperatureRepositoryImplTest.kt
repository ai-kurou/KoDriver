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
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeTemperatureRepositoryImplTest {
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
    fun `共有メモリから4輪のブレーキ温度をCelsiusのまま読み取る`() =
        runBlocking {
            val reader =
                FakeBrakeTemperatureMemoryReader(
                    buildBrakeTemperatureBuffer(
                        BrakeTemperatureBufferConfig(
                            temperatures =
                                mapOf(
                                    WheelIndex.FRONT_LEFT to 450.0,
                                    WheelIndex.FRONT_RIGHT to 451.0,
                                    WheelIndex.REAR_LEFT to 452.0,
                                    WheelIndex.REAR_RIGHT to 453.0,
                                ),
                        ),
                    ),
                )
            val repo = LmuWindowsBrakeTemperatureRepositoryImpl(source = makeSource(reader))

            val result = repo.brakeTemperatureStream().first()

            assertEquals(450.0f, result.wheels[WheelIndex.FRONT_LEFT]!!.value, 1e-4f)
            assertEquals(451.0f, result.wheels[WheelIndex.FRONT_RIGHT]!!.value, 1e-4f)
            assertEquals(452.0f, result.wheels[WheelIndex.REAR_LEFT]!!.value, 1e-4f)
            assertEquals(453.0f, result.wheels[WheelIndex.REAR_RIGHT]!!.value, 1e-4f)
        }

    @Test
    fun `playerIndexに応じた車両スロットからブレーキ温度を読み取る`() =
        runBlocking {
            val reader =
                FakeBrakeTemperatureMemoryReader(
                    buildBrakeTemperatureBuffer(
                        BrakeTemperatureBufferConfig(
                            activeVehicles = 2,
                            playerIdx = 1,
                            temperatures = mapOf(WheelIndex.FRONT_LEFT to 512.0),
                        ),
                    ),
                )
            val repo = LmuWindowsBrakeTemperatureRepositoryImpl(source = makeSource(reader))

            val result = repo.brakeTemperatureStream().first()

            assertEquals(512.0f, result.wheels[WheelIndex.FRONT_LEFT]!!.value, 1e-4f)
        }

    @Test
    fun `activeVehicles が 0 のとき emit しない`() =
        runBlocking {
            val reader =
                FakeBrakeTemperatureMemoryReader(
                    buildBrakeTemperatureBuffer(BrakeTemperatureBufferConfig(activeVehicles = 0)),
                )
            val repo = LmuWindowsBrakeTemperatureRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.brakeTemperatureStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    @Test
    fun `playerIdxがactiveVehicles以上のとき emit しない`() =
        runBlocking {
            val reader =
                FakeBrakeTemperatureMemoryReader(
                    buildBrakeTemperatureBuffer(
                        BrakeTemperatureBufferConfig(activeVehicles = 1, playerIdx = 1),
                    ),
                )
            val repo = LmuWindowsBrakeTemperatureRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.brakeTemperatureStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    @Test
    fun `reader が open できない間は emit しない`() =
        runBlocking {
            val reader =
                FakeBrakeTemperatureMemoryReader(
                    buffer = buildBrakeTemperatureBuffer(),
                    openResult = false,
                )
            val repo = LmuWindowsBrakeTemperatureRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.brakeTemperatureStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    private fun buildBrakeTemperatureBuffer(
        config: BrakeTemperatureBufferConfig = BrakeTemperatureBufferConfig(),
    ): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, config.activeVehicles.toByte())
        buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, config.playerIdx.toByte())

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + config.playerIdx * VEHICLE_STRIDE
        WheelIndex.entries.forEach { wheel ->
            val wheelBase = vehicleBase + OFF_WHEELS + wheel.ordinal * WHEEL_STRIDE
            buffer.putDouble(wheelBase + OFF_WHEEL_BRAKE_TEMP, config.temperatures[wheel] ?: 0.0)
        }

        return buffer
    }

    private data class BrakeTemperatureBufferConfig(
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
        const val OFF_WHEEL_BRAKE_TEMP = 24
        const val BUFFER_SIZE = 135_000
    }
}

private class FakeBrakeTemperatureMemoryReader(
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
