package kurou.kodriver.core.gt7ps5data.mapper

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5MapperTest {

    private companion object {
        const val LAP_COUNT_OFFSET = 0x74
        const val LAPS_IN_RACE_OFFSET = 0x76
        const val BEST_LAP_TIME_OFFSET = 0x78
        const val GAS_LEVEL_OFFSET = 0x84
        const val GAS_CAPACITY_OFFSET = 0x88
        const val ENERGY_RECOVERY_OFFSET = 0xC4
        const val PACKET_SIZE = 0x128

        fun packetWith(
            lapCount: Short = 0,
            lapsInRace: Short = 0,
            bestLapTimeMs: Int = -1,
            gasLevel: Float = 0f,
            gasCapacity: Float = 100f,
            energyRecovery: Float = 0f,
        ): ByteBuffer {
            val buf = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            buf.putShort(LAP_COUNT_OFFSET, lapCount)
            buf.putShort(LAPS_IN_RACE_OFFSET, lapsInRace)
            buf.putInt(BEST_LAP_TIME_OFFSET, bestLapTimeMs)
            buf.putFloat(GAS_LEVEL_OFFSET, gasLevel)
            buf.putFloat(GAS_CAPACITY_OFFSET, gasCapacity)
            buf.putFloat(ENERGY_RECOVERY_OFFSET, energyRecovery)
            return buf
        }
    }

    @Test
    fun `LapCountを正しくマッピングする`() {
        val packet = packetWith(lapCount = 5)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(5, result.lapCount)
    }

    @Test
    fun `LapsInRaceを正しくマッピングする`() {
        val packet = packetWith(lapsInRace = 20)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(20, result.lapsInRace)
    }

    @Test
    fun `BestLapTimeMsを正しくマッピングする`() {
        val packet = packetWith(bestLapTimeMs = 90_000)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(90_000, result.bestLapTimeMs)
    }

    @Test
    fun `ベストラップなしの場合はマイナス1を返す`() {
        val packet = packetWith(bestLapTimeMs = -1)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(-1, result.bestLapTimeMs)
    }

    @Test
    fun `GasLevelを正しくマッピングする`() {
        val packet = packetWith(gasLevel = 45.5f)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(45.5f, result.gasLevel)
    }

    @Test
    fun `GasCapacityを正しくマッピングする`() {
        val packet = packetWith(gasCapacity = 100f)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(100f, result.gasCapacity)
    }

    @Test
    fun `EnergyRecoveryを正しくマッピングする`() {
        val packet = packetWith(energyRecovery = 0.75f)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(0.75f, result.energyRecovery)
    }

    @Test
    fun `全フィールドを同時に正しくマッピングする`() {
        val packet = packetWith(
            lapCount = 3,
            lapsInRace = 15,
            bestLapTimeMs = 85_432,
            gasLevel = 30.2f,
            gasCapacity = 80f,
            energyRecovery = 0.5f,
        )
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(3, result.lapCount)
        assertEquals(15, result.lapsInRace)
        assertEquals(85_432, result.bestLapTimeMs)
        assertEquals(30.2f, result.gasLevel)
        assertEquals(80f, result.gasCapacity)
        assertEquals(0.5f, result.energyRecovery)
    }
}
