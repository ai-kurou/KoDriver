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

class LmuWindowsCarGapRepositoryImplTest {
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
    fun `Scoringの自車がなければ全体ギャップはnullで生値を返す`() =
        runBlocking {
            val reader =
                FakeCarGapMemoryReader(
                    buildCarGapBuffer(CarGapBufferConfig()),
                )
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(reader))

            val result = repo.carGapStream().first()

            assertEquals(1.25, result.timeGapCarAheadSeconds)
            assertEquals(2.5, result.timeGapCarBehindSeconds)
            assertEquals(3.75, result.timeGapPlaceAheadSeconds)
            assertEquals(4.5, result.timeGapPlaceBehindSeconds)
            assertEquals(null, result.overallGapAheadSeconds)
            assertEquals(null, result.overallGapBehindSeconds)
        }

    @Test
    fun `Telemetryとは別のScoring自車スロットから全体ギャップを計算する`() =
        runBlocking {
            val buffer = buildCarGapBuffer()
            buffer.putInt(1632 + 104, 2)
            val playerBase = 2192 + 584
            buffer.put(playerBase + 196, 1.toByte())
            buffer.putDouble(playerBase + 464, 20.0)
            buffer.putDouble(playerBase + 472, 100.0)
            buffer.putDouble(2192 + 464, 25.0)
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(FakeCarGapMemoryReader(buffer)))

            val result = repo.carGapStream().first()

            assertEquals(1.25, result.timeGapCarAheadSeconds)
            assertEquals(2.5, result.timeGapCarBehindSeconds)
            assertEquals(3.75, result.timeGapPlaceAheadSeconds)
            assertEquals(4.5, result.timeGapPlaceBehindSeconds)
            assertEquals(5.0, result.overallGapAheadSeconds)
            assertEquals(95.0, result.overallGapBehindSeconds)
        }

    @Test
    fun `playerIndexに応じた車両スロットから4種類の車両時間差を読み取る`() =
        runBlocking {
            val reader =
                FakeCarGapMemoryReader(
                    buildCarGapBuffer(
                        CarGapBufferConfig(activeVehicles = 2, playerIdx = 1),
                    ),
                )
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(reader))

            val result = repo.carGapStream().first()

            assertEquals(1.25, result.timeGapCarAheadSeconds)
            assertEquals(2.5, result.timeGapCarBehindSeconds)
            assertEquals(3.75, result.timeGapPlaceAheadSeconds)
            assertEquals(4.5, result.timeGapPlaceBehindSeconds)
            assertEquals(null, result.overallGapAheadSeconds)
            assertEquals(null, result.overallGapBehindSeconds)
        }

    @Test
    fun `activeVehicles が 0 のとき emit しない`() =
        runBlocking {
            val reader =
                FakeCarGapMemoryReader(
                    buildCarGapBuffer(CarGapBufferConfig(activeVehicles = 0)),
                )
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.carGapStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    @Test
    fun `playerIdxがactiveVehicles以上のとき emit しない`() =
        runBlocking {
            val reader =
                FakeCarGapMemoryReader(
                    buildCarGapBuffer(CarGapBufferConfig(activeVehicles = 1, playerIdx = 1)),
                )
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.carGapStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    @Test
    fun `reader が open できない間は emit しない`() =
        runBlocking {
            val reader =
                FakeCarGapMemoryReader(
                    buffer = buildCarGapBuffer(),
                    openResult = false,
                )
            val repo = LmuWindowsCarGapRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.carGapStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    private fun buildCarGapBuffer(config: CarGapBufferConfig = CarGapBufferConfig()): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, config.activeVehicles.toByte())
        buffer.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, config.playerIdx.toByte())

        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + config.playerIdx * VEHICLE_STRIDE
        buffer.putFloat(vehicleBase + OFF_TIME_GAP_CAR_AHEAD, config.timeGapCarAheadSeconds)
        buffer.putFloat(vehicleBase + OFF_TIME_GAP_CAR_BEHIND, config.timeGapCarBehindSeconds)
        buffer.putFloat(vehicleBase + OFF_TIME_GAP_PLACE_AHEAD, config.timeGapPlaceAheadSeconds)
        buffer.putFloat(vehicleBase + OFF_TIME_GAP_PLACE_BEHIND, config.timeGapPlaceBehindSeconds)

        return buffer
    }

    private data class CarGapBufferConfig(
        val activeVehicles: Int = 1,
        val playerIdx: Int = 0,
        val timeGapCarAheadSeconds: Float = 1.25f,
        val timeGapCarBehindSeconds: Float = 2.5f,
        val timeGapPlaceAheadSeconds: Float = 3.75f,
        val timeGapPlaceBehindSeconds: Float = 4.5f,
    )

    private companion object {
        const val TELEMETRY_BASE = 128_464
        const val OFF_ACTIVE_VEHICLES = 0
        const val OFF_PLAYER_VEHICLE_IDX = 1
        const val OFF_TELEM_INFO = 4
        const val VEHICLE_STRIDE = 1_888
        const val OFF_TIME_GAP_CAR_AHEAD = 780
        const val OFF_TIME_GAP_CAR_BEHIND = 784
        const val OFF_TIME_GAP_PLACE_AHEAD = 788
        const val OFF_TIME_GAP_PLACE_BEHIND = 792
        const val BUFFER_SIZE = 135_000
    }
}

private class FakeCarGapMemoryReader(
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
