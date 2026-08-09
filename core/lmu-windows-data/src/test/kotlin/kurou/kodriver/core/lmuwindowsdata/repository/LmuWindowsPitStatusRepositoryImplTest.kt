@file:Suppress("FunctionNaming")

package kurou.kodriver.core.lmuwindowsdata.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.core.lmuwindowsdata.datasource.LmuWindowsSharedMemorySource
import kurou.kodriver.core.model.LmuWindowsPitState
import kurou.kodriver.core.windowssharedmemory.datasource.SharedMemoryReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsPitStatusRepositoryImplTest {
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
    fun `Scoring セグメントからプレイヤー車両のピット状態を読み取る`() =
        runBlocking {
            val reader =
                FakePitStatusMemoryReader(
                    buildPitStatusBuffer(PitStatusBufferConfig(inPits = true, pitState = 2, inGarageStall = false)),
                )
            val repo = LmuWindowsPitStatusRepositoryImpl(source = makeSource(reader))

            val result = repo.pitStatusStream().first()

            assertTrue(result.inPits)
            assertEquals(LmuWindowsPitState.ENTERING, result.pitState)
            assertFalse(result.inGarageStall)
        }

    @Test
    fun `mIsPlayer な車両が存在しないときは既定値を emit する`() =
        runBlocking {
            val reader =
                FakePitStatusMemoryReader(
                    buildPitStatusBuffer(PitStatusBufferConfig(numVehicles = 2, playerIndex = -1)),
                )
            val repo = LmuWindowsPitStatusRepositoryImpl(source = makeSource(reader))

            val result = repo.pitStatusStream().first()

            assertFalse(result.inPits)
            assertEquals(LmuWindowsPitState.UNKNOWN, result.pitState)
            assertFalse(result.inGarageStall)
        }

    private fun buildPitStatusBuffer(config: PitStatusBufferConfig = PitStatusBufferConfig()): ByteBuffer {
        val buffer = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, config.numVehicles)

        if (config.playerIndex in 0 until config.numVehicles) {
            val vehicleBase = VEHICLE_SCORING_BASE + config.playerIndex * VEHICLE_SCORING_STRIDE
            buffer.put(vehicleBase + OFF_SCORING_IS_PLAYER, 1)
            buffer.put(vehicleBase + OFF_SCORING_IN_PITS, if (config.inPits) 1 else 0)
            buffer.put(vehicleBase + OFF_SCORING_PIT_STATE, config.pitState.toByte())
            buffer.put(vehicleBase + OFF_SCORING_IN_GARAGE_STALL, if (config.inGarageStall) 1 else 0)
        }

        return buffer
    }

    private data class PitStatusBufferConfig(
        val numVehicles: Int = 1,
        val playerIndex: Int = 0,
        val inPits: Boolean = false,
        val pitState: Int = 0,
        val inGarageStall: Boolean = false,
    )

    private companion object {
        const val SCORING_BASE = 1_632
        const val OFF_SCORING_NUM_VEHICLES = 104
        const val VEHICLE_SCORING_BASE = 2_192
        const val VEHICLE_SCORING_STRIDE = 584
        const val OFF_SCORING_IS_PLAYER = 196
        const val OFF_SCORING_IN_PITS = 198
        const val OFF_SCORING_PIT_STATE = 457
        const val OFF_SCORING_IN_GARAGE_STALL = 507
        const val BUFFER_SIZE = 135_000
    }
}

private class FakePitStatusMemoryReader(
    private val buffer: ByteBuffer,
) : SharedMemoryReader {
    private var opened = true

    override fun open(): Boolean {
        opened = true
        return true
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
