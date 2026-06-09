package kurou.kodriver.data.repository

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.CountLapFlag
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.SessionYellowFlagState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedMemoryFlagRepositoryTest {

    @Test
    fun `共有メモリからセッション旗とプレイヤー旗を読み取る`() = runBlocking {
        val reader = FakeStaticMemoryReader(
            buildFlagsBuffer(
                FlagBufferConfig(
                    gamePhase = 4,
                    yellowFlagState = 2,
                    sectorFlags = listOf(0, 2, 0),
                    startLight = 3,
                    numRedLights = 5,
                    playerFlag = 6,
                    playerUnderYellow = true,
                    playerCountLapFlag = 1,
                ),
            ),
        )
        val repo = SharedMemoryFlagRepository(
            pollingIntervalMs = 1,
            reader = reader,
        )

        val result = repo.flagStream().first()

        assertEquals(SessionPhase.COUNTDOWN, result.gamePhase)
        assertEquals(SessionYellowFlagState.PIT_CLOSED, result.yellowFlagState)
        assertEquals(listOf(SectorFlagState.CLEAR, SectorFlagState.UNKNOWN, SectorFlagState.CLEAR), result.sectorFlags)
        assertEquals(3, result.startLight)
        assertEquals(5, result.numRedLights)
        assertEquals(PrimaryFlag.CHECKERED, result.playerFlag)
        assertTrue(result.playerUnderYellow)
        assertEquals(CountLapFlag.COUNT, result.playerCountLapFlag)
    }

    @Test
    fun `player が見つからない間は emit しない`() = runBlocking {
        val reader = FakeStaticMemoryReader(buildFlagsBuffer(FlagBufferConfig(hasPlayer = false)))
        val repo = SharedMemoryFlagRepository(
            pollingIntervalMs = 1,
            reader = reader,
        )
        val emitCount = AtomicInteger(0)

        val job = launch { repo.flagStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
        assertTrue(reader.closeCalled)
    }

    @Test
    fun `reader が open できない間は emit しない`() = runBlocking {
        val reader = FakeStaticMemoryReader(
            buffer = buildFlagsBuffer(),
            initialOpen = false,
            openResult = false,
        )
        val repo = SharedMemoryFlagRepository(
            pollingIntervalMs = 1,
            reconnectIntervalMs = 1,
            reader = reader,
        )
        val emitCount = AtomicInteger(0)

        val job = launch { repo.flagStream().collect { emitCount.incrementAndGet() } }
        delay(50)
        job.cancelAndJoin()

        assertEquals(0, emitCount.get())
        assertTrue(reader.closeCalled)
    }

    @Test
    fun `フローがキャンセルされると reader の close が呼ばれる`() = runBlocking {
        val reader = FakeStaticMemoryReader(buildFlagsBuffer())
        val repo = SharedMemoryFlagRepository(
            pollingIntervalMs = 1,
            reader = reader,
        )

        val job = launch { repo.flagStream().collect { } }
        delay(50)
        job.cancelAndJoin()

        assertTrue(reader.closeCalled)
    }

    private fun buildFlagsBuffer(config: FlagBufferConfig = FlagBufferConfig()): ByteBuffer {
        val buffer = ByteBuffer.allocate(135_000).order(ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(1_632 + 104, config.vehicleCount)
        buffer.put(1_632 + 108, config.gamePhase.toByte())
        buffer.put(1_632 + 109, config.yellowFlagState.toByte())
        buffer.put(1_632 + 110, config.sectorFlags[0].toByte())
        buffer.put(1_632 + 111, config.sectorFlags[1].toByte())
        buffer.put(1_632 + 112, config.sectorFlags[2].toByte())
        buffer.put(1_632 + 113, config.startLight.toByte())
        buffer.put(1_632 + 114, config.numRedLights.toByte())

        if (config.hasPlayer) {
            val playerBase = 2_192 + 584
            buffer.put(playerBase + 196, 1.toByte())
            buffer.put(playerBase + 504, config.playerFlag.toByte())
            buffer.put(playerBase + 505, if (config.playerUnderYellow) 1.toByte() else 0.toByte())
            buffer.put(playerBase + 506, config.playerCountLapFlag.toByte())
        }

        return buffer
    }

    private data class FlagBufferConfig(
        val vehicleCount: Int = 3,
        val hasPlayer: Boolean = true,
        val gamePhase: Int = 0,
        val yellowFlagState: Int = 0,
        val sectorFlags: List<Int> = listOf(0, 0, 0),
        val startLight: Int = 0,
        val numRedLights: Int = 0,
        val playerFlag: Int = 0,
        val playerUnderYellow: Boolean = false,
        val playerCountLapFlag: Int = 0,
    )
}
