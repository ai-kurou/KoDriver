package kurou.kodriver.core.gt7ps5data.mapper

import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.Gt7Ps5FuelUnit
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5MapperTest {
    private companion object {
        const val LAP_COUNT_OFFSET = 0x74
        const val LAPS_IN_RACE_OFFSET = 0x76
        const val BEST_LAP_TIME_OFFSET = 0x78
        const val GAS_LEVEL_OFFSET = 0x44
        const val GAS_CAPACITY_OFFSET = 0x48
        const val TYRE_TEMP_FRONT_LEFT_OFFSET = 0x60
        const val TYRE_TEMP_FRONT_RIGHT_OFFSET = 0x64
        const val TYRE_TEMP_REAR_LEFT_OFFSET = 0x68
        const val TYRE_TEMP_REAR_RIGHT_OFFSET = 0x6C
        const val CAR_CATEGORY_OFFSET = 0x16C
        const val PACKET_SIZE = 0x170

        data class TyreTemps(
            val frontLeft: Float = 0f,
            val frontRight: Float = 0f,
            val rearLeft: Float = 0f,
            val rearRight: Float = 0f,
        )

        fun packetWith(
            lapCount: Short = 0,
            lapsInRace: Short = 0,
            bestLapTimeMs: Int = -1,
            gasLevel: Float = 0f,
            gasCapacity: Float = 100f,
            tyreTemps: TyreTemps = TyreTemps(),
            carCategory: String? = null,
        ): ByteBuffer {
            val buf = ByteBuffer.allocate(PACKET_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            buf.putShort(LAP_COUNT_OFFSET, lapCount)
            buf.putShort(LAPS_IN_RACE_OFFSET, lapsInRace)
            buf.putInt(BEST_LAP_TIME_OFFSET, bestLapTimeMs)
            buf.putFloat(GAS_LEVEL_OFFSET, gasLevel)
            buf.putFloat(GAS_CAPACITY_OFFSET, gasCapacity)
            buf.putFloat(TYRE_TEMP_FRONT_LEFT_OFFSET, tyreTemps.frontLeft)
            buf.putFloat(TYRE_TEMP_FRONT_RIGHT_OFFSET, tyreTemps.frontRight)
            buf.putFloat(TYRE_TEMP_REAR_LEFT_OFFSET, tyreTemps.rearLeft)
            buf.putFloat(TYRE_TEMP_REAR_RIGHT_OFFSET, tyreTemps.rearRight)
            carCategory?.let { value ->
                value.forEachIndexed { index, char -> buf.put(CAR_CATEGORY_OFFSET + index, char.code.toByte()) }
            }
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
        assertEquals(Gt7Ps5FuelUnit(45.5f), result.gasLevel)
    }

    @Test
    fun `GasCapacityを正しくマッピングする`() {
        val packet = packetWith(gasCapacity = 100f)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(Gt7Ps5FuelUnit(100f), result.gasCapacity)
    }

    @Test
    fun `全フィールドを同時に正しくマッピングする`() {
        val packet =
            packetWith(
                lapCount = 3,
                lapsInRace = 15,
                bestLapTimeMs = 85_432,
                gasLevel = 30.2f,
                gasCapacity = 80f,
                tyreTemps = TyreTemps(frontLeft = 82.1f, frontRight = 83.4f, rearLeft = 78.5f, rearRight = 79.9f),
                carCategory = "GR3",
            )
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(3, result.lapCount)
        assertEquals(15, result.lapsInRace)
        assertEquals(85_432, result.bestLapTimeMs)
        assertEquals(Gt7Ps5FuelUnit(30.2f), result.gasLevel)
        assertEquals(Gt7Ps5FuelUnit(80f), result.gasCapacity)
        assertEquals(CelsiusReading(82.1f), result.tyreTemperature.frontLeftCelsius)
        assertEquals(CelsiusReading(83.4f), result.tyreTemperature.frontRightCelsius)
        assertEquals(CelsiusReading(78.5f), result.tyreTemperature.rearLeftCelsius)
        assertEquals(CelsiusReading(79.9f), result.tyreTemperature.rearRightCelsius)
        assertEquals("GR3", result.carCategory)
    }

    @Test
    fun `TyreTemperatureFLを正しくマッピングする`() {
        val packet = packetWith(tyreTemps = TyreTemps(frontLeft = 90.5f))
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(CelsiusReading(90.5f), result.tyreTemperature.frontLeftCelsius)
    }

    @Test
    fun `TyreTemperatureFRを正しくマッピングする`() {
        val packet = packetWith(tyreTemps = TyreTemps(frontRight = 91.5f))
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(CelsiusReading(91.5f), result.tyreTemperature.frontRightCelsius)
    }

    @Test
    fun `TyreTemperatureRLを正しくマッピングする`() {
        val packet = packetWith(tyreTemps = TyreTemps(rearLeft = 85.5f))
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(CelsiusReading(85.5f), result.tyreTemperature.rearLeftCelsius)
    }

    @Test
    fun `TyreTemperatureRRを正しくマッピングする`() {
        val packet = packetWith(tyreTemps = TyreTemps(rearRight = 86.5f))
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals(CelsiusReading(86.5f), result.tyreTemperature.rearRightCelsius)
    }

    @Test
    fun `CarCategoryをNULL終端でトリムしてマッピングする`() {
        val packet = packetWith(carCategory = "GR3")
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals("GR3", result.carCategory)
    }

    @Test
    fun `CarCategoryが4文字でNULL終端がない場合はそのままマッピングする`() {
        val packet = packetWith(carCategory = "GRX4")
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals("GRX4", result.carCategory)
    }

    @Test
    fun `CarCategoryが全てNULLバイトの場合は空文字列を返す`() {
        val packet = packetWith(carCategory = null)
        val result = Gt7Ps5Mapper.map(packet)
        assertEquals("", result.carCategory)
    }
}
