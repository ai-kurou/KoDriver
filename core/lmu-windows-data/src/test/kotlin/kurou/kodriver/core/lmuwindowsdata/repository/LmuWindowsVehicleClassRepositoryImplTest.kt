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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassRepositoryImplTest {
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
    fun `Scoring セグメントからプレイヤー車両のクラス名を読み取る`() =
        runBlocking {
            val reader =
                FakeVehicleClassMemoryReader(
                    buildVehicleClassBuffer(VehicleClassBufferConfig(vehicleClass = "Hypercar")),
                )
            val repo = LmuWindowsVehicleClassRepositoryImpl(source = makeSource(reader))

            val result = repo.vehicleClassStream().first()

            assertEquals("Hypercar", result.name)
        }

    @Test
    fun `mIsPlayer が立っているインデックスの車両からクラス名を読み取る`() =
        runBlocking {
            val reader =
                FakeVehicleClassMemoryReader(
                    buildVehicleClassBuffer(
                        VehicleClassBufferConfig(numVehicles = 3, playerIndex = 2, vehicleClass = "LMP2"),
                    ),
                )
            val repo = LmuWindowsVehicleClassRepositoryImpl(source = makeSource(reader))

            val result = repo.vehicleClassStream().first()

            assertEquals("LMP2", result.name)
        }

    @Test
    fun `numVehicles が 0 のときは空文字列を emit する`() =
        runBlocking {
            val reader =
                FakeVehicleClassMemoryReader(
                    buildVehicleClassBuffer(VehicleClassBufferConfig(numVehicles = 0)),
                )
            val repo = LmuWindowsVehicleClassRepositoryImpl(source = makeSource(reader))

            val result = repo.vehicleClassStream().first()

            assertEquals("", result.name)
        }

    @Test
    fun `mIsPlayer な車両が存在しないときは空文字列を emit する`() =
        runBlocking {
            val reader =
                FakeVehicleClassMemoryReader(
                    buildVehicleClassBuffer(VehicleClassBufferConfig(numVehicles = 2, playerIndex = -1)),
                )
            val repo = LmuWindowsVehicleClassRepositoryImpl(source = makeSource(reader))

            val result = repo.vehicleClassStream().first()

            assertEquals("", result.name)
        }

    @Test
    fun `reader が open できない間は emit しない`() =
        runBlocking {
            val reader =
                FakeVehicleClassMemoryReader(
                    buffer = buildVehicleClassBuffer(),
                    openResult = false,
                )
            val repo = LmuWindowsVehicleClassRepositoryImpl(source = makeSource(reader))
            val emitCount = AtomicInteger(0)

            val job = launch { repo.vehicleClassStream().collect { emitCount.incrementAndGet() } }
            delay(50)
            job.cancelAndJoin()

            assertEquals(0, emitCount.get())
        }

    private fun buildVehicleClassBuffer(config: VehicleClassBufferConfig = VehicleClassBufferConfig()): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, config.numVehicles)

        if (config.playerIndex in 0 until config.numVehicles) {
            val vehicleBase = VEHICLE_SCORING_BASE + config.playerIndex * VEHICLE_SCORING_STRIDE
            buffer.put(vehicleBase + OFF_SCORING_IS_PLAYER, 1)
            val classBytes = config.vehicleClass.toByteArray(StandardCharsets.US_ASCII)
            classBytes.forEachIndexed { index, byte ->
                buffer.put(vehicleBase + OFF_SCORING_VEHICLE_CLASS + index, byte)
            }
        }

        return buffer
    }

    private data class VehicleClassBufferConfig(
        val numVehicles: Int = 1,
        val playerIndex: Int = 0,
        val vehicleClass: String = "",
    )

    private companion object {
        const val SCORING_BASE = 1_632
        const val OFF_SCORING_NUM_VEHICLES = 104
        const val VEHICLE_SCORING_BASE = 2_192
        const val VEHICLE_SCORING_STRIDE = 584
        const val OFF_SCORING_IS_PLAYER = 196
        const val OFF_SCORING_VEHICLE_CLASS = 200
        const val BUFFER_SIZE = 135_000
    }
}

private class FakeVehicleClassMemoryReader(
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
