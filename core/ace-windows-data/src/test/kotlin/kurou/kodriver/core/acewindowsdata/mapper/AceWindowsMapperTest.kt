package kurou.kodriver.core.acewindowsdata.mapper

import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AceWindowsMapperTest {
    private companion object {
        const val OFF_STATUS = 4
        const val OFF_PLAYER_CAR_ID_A = 24
        const val OFF_PLAYER_CAR_ID_B = 32
        const val OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT = 200
        const val OFF_TYRE_LF = 220
        const val TYRE_STATE_STRIDE = 256
        const val OFF_TYRE_TEMPERATURE_C = 12
        const val OFF_CAR_LOCATION = 1388
        const val OFF_FLAG = 2404
        const val OFF_CAR_COORDINATES = 3124
        const val CAR_COORDINATES_STRIDE = 12
        const val OFF_ACTIVE_CARS = 3852
        const val OFF_CAR_IDS = 3940
        const val CAR_ID_STRIDE = 16
        const val BUFFER_SIZE = 8_192
    }

    private data class CarEntry(
        val idA: Long,
        val idB: Long,
        val x: Float,
        val y: Float,
        val z: Float,
    )

    private fun vehicleApproachBuffer(
        cars: List<CarEntry>,
        playerCarIdA: Long,
        playerCarIdB: Long,
        activeCars: Int = cars.size,
    ): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also { buffer ->
            buffer.putLong(OFF_PLAYER_CAR_ID_A, playerCarIdA)
            buffer.putLong(OFF_PLAYER_CAR_ID_B, playerCarIdB)
            buffer.put(OFF_ACTIVE_CARS, activeCars.toByte())
            cars.forEachIndexed { i, car ->
                val coordinatesBase = OFF_CAR_COORDINATES + i * CAR_COORDINATES_STRIDE
                buffer.putFloat(coordinatesBase, car.x)
                buffer.putFloat(coordinatesBase + Float.SIZE_BYTES, car.y)
                buffer.putFloat(coordinatesBase + Float.SIZE_BYTES * 2, car.z)
                val idsBase = OFF_CAR_IDS + i * CAR_ID_STRIDE
                buffer.putLong(idsBase, car.idA)
                buffer.putLong(idsBase + Long.SIZE_BYTES, car.idB)
            }
        }

    private fun tyreCarcassTemperatureBuffer(temperatures: Map<WheelIndex, Float>): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also { buffer ->
            temperatures.forEach { (wheel, celsius) ->
                val tyreStateBase = OFF_TYRE_LF + wheel.ordinal * TYRE_STATE_STRIDE
                buffer.putFloat(tyreStateBase + OFF_TYRE_TEMPERATURE_C, celsius)
            }
        }

    private fun buffer(fuelPercent: Float): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also {
            it.putFloat(OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT, fuelPercent)
        }

    private fun flagBuffer(flagRawValue: Int): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also {
            it.putInt(OFF_FLAG, flagRawValue)
        }

    private fun statusBuffer(
        statusRawValue: Int,
        carLocationRawValue: Int = AceWindowsCarLocation.UNASSIGNED.rawValue,
    ): ByteBuffer =
        ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN).also {
            it.putInt(OFF_STATUS, statusRawValue)
            it.putInt(OFF_CAR_LOCATION, carLocationRawValue)
        }

    @Test
    fun `fuel_liter_current_quantity_percent の割合を100倍してremainingPercentとして取得する`() {
        val result = AceWindowsMapper.mapFuel(buffer(0.75f))

        assertEquals(75.0, result.remainingPercent.value, 0.0001)
    }

    @Test
    fun `満タンのとき remainingPercent は100を返す`() {
        val result = AceWindowsMapper.mapFuel(buffer(1.0f))

        assertEquals(FuelPercent(100.0), result.remainingPercent)
    }

    @Test
    fun `残燃料0のとき remainingPercent は0を返す`() {
        val result = AceWindowsMapper.mapFuel(buffer(0.0f))

        assertEquals(FuelPercent(0.0), result.remainingPercent)
    }

    @Test
    fun `4輪それぞれのtyre_temperature_cをカーカス平均温度として取得する`() {
        val temperatures =
            mapOf(
                WheelIndex.FRONT_LEFT to 80.0f,
                WheelIndex.FRONT_RIGHT to 81.0f,
                WheelIndex.REAR_LEFT to 82.0f,
                WheelIndex.REAR_RIGHT to 83.0f,
            )

        val result = AceWindowsMapper.mapTyreCarcassTemperature(tyreCarcassTemperatureBuffer(temperatures))

        assertEquals(CelsiusReading(80.0f), result.wheels[WheelIndex.FRONT_LEFT])
        assertEquals(CelsiusReading(81.0f), result.wheels[WheelIndex.FRONT_RIGHT])
        assertEquals(CelsiusReading(82.0f), result.wheels[WheelIndex.REAR_LEFT])
        assertEquals(CelsiusReading(83.0f), result.wheels[WheelIndex.REAR_RIGHT])
    }

    @Test
    fun `flag は全てのACEVO_FLAG_TYPEの値を取得できる`() {
        val expected =
            mapOf(
                0 to AceWindowsFlagType.NO_FLAG,
                1 to AceWindowsFlagType.WHITE_FLAG,
                2 to AceWindowsFlagType.GREEN_FLAG,
                3 to AceWindowsFlagType.RED_FLAG,
                4 to AceWindowsFlagType.BLUE_FLAG,
                5 to AceWindowsFlagType.YELLOW_FLAG,
                6 to AceWindowsFlagType.BLACK_FLAG,
                7 to AceWindowsFlagType.BLACK_WHITE_FLAG,
                8 to AceWindowsFlagType.CHECKERED_FLAG,
                9 to AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                10 to AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
            )

        expected.forEach { (rawValue, flagType) ->
            val result = AceWindowsMapper.mapFlag(flagBuffer(rawValue))

            assertEquals(flagType, result.flag, "rawValue=$rawValue")
        }
    }

    @Test
    fun `flag が未知の値のとき UNKNOWN を返す`() {
        val result = AceWindowsMapper.mapFlag(flagBuffer(-1))

        assertEquals(AceWindowsFlagType.UNKNOWN, result.flag)
    }

    @Test
    fun `status は全てのACEVO_STATUSの値を取得できる`() {
        val expected =
            mapOf(
                0 to AceWindowsStatusType.OFF,
                1 to AceWindowsStatusType.REPLAY,
                2 to AceWindowsStatusType.LIVE,
                3 to AceWindowsStatusType.PAUSE,
            )

        expected.forEach { (rawValue, statusType) ->
            val result = AceWindowsMapper.mapStatus(statusBuffer(rawValue))

            assertEquals(statusType, result.status, "rawValue=$rawValue")
        }
    }

    @Test
    fun `status が未知の値のとき UNKNOWN を返す`() {
        val result = AceWindowsMapper.mapStatus(statusBuffer(-1))

        assertEquals(AceWindowsStatusType.UNKNOWN, result.status)
    }

    @Test
    fun `carLocation は全てのACEVO_CAR_LOCATIONの値を取得できる`() {
        val expected =
            mapOf(
                0 to AceWindowsCarLocation.UNASSIGNED,
                1 to AceWindowsCarLocation.PITLANE,
                2 to AceWindowsCarLocation.PITENTRY,
                3 to AceWindowsCarLocation.PITEXIT,
                4 to AceWindowsCarLocation.TRACK,
            )

        expected.forEach { (rawValue, carLocation) ->
            val result = AceWindowsMapper.mapStatus(statusBuffer(AceWindowsStatusType.LIVE.rawValue, rawValue))

            assertEquals(carLocation, result.carLocation, "rawValue=$rawValue")
        }
    }

    @Test
    fun `carLocation が未知の値のとき UNKNOWN を返す`() {
        val result = AceWindowsMapper.mapStatus(statusBuffer(AceWindowsStatusType.LIVE.rawValue, -1))

        assertEquals(AceWindowsCarLocation.UNKNOWN, result.carLocation)
    }

    @Test
    fun `自車以外の各アクティブ車両との直線距離を取得する`() {
        val cars =
            listOf(
                CarEntry(idA = 1L, idB = 2L, x = 0f, y = 0f, z = 0f),
                CarEntry(idA = 3L, idB = 4L, x = 3f, y = 0f, z = 4f),
                CarEntry(idA = 5L, idB = 6L, x = 0f, y = 0f, z = -10f),
            )
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertEquals(2, result.nearbyVehicles.size)
        assertEquals(5.0, result.nearbyVehicles[0].distanceMeters, 0.0001)
        assertEquals(10.0, result.nearbyVehicles[1].distanceMeters, 0.0001)
    }

    @Test
    fun `Y座標の差も直線距離に含める`() {
        val cars =
            listOf(
                CarEntry(idA = 1L, idB = 2L, x = 0f, y = 0f, z = 0f),
                CarEntry(idA = 3L, idB = 4L, x = 0f, y = 3f, z = 4f),
            )
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertEquals(1, result.nearbyVehicles.size)
        assertEquals(5.0, result.nearbyVehicles[0].distanceMeters, 0.0001)
    }

    @Test
    fun `自車のIDがcar_idsに見つからない場合は空リストを返す`() {
        val cars = listOf(CarEntry(idA = 3L, idB = 4L, x = 0f, y = 0f, z = 0f))
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertTrue(result.nearbyVehicles.isEmpty())
    }

    @Test
    fun `active_cars が0のとき空リストを返す`() {
        val cars = listOf(CarEntry(idA = 1L, idB = 2L, x = 0f, y = 0f, z = 0f))
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L, activeCars = 0)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertTrue(result.nearbyVehicles.isEmpty())
    }

    @Test
    fun `自車のみがアクティブなとき空リストを返す`() {
        val cars = listOf(CarEntry(idA = 1L, idB = 2L, x = 5f, y = 5f, z = 5f))
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertTrue(result.nearbyVehicles.isEmpty())
    }

    @Test
    fun `距離計算はsqrtでユークリッド距離として求められる`() {
        val cars =
            listOf(
                CarEntry(idA = 1L, idB = 2L, x = 0f, y = 0f, z = 0f),
                CarEntry(idA = 3L, idB = 4L, x = 1f, y = 1f, z = 1f),
            )
        val buffer = vehicleApproachBuffer(cars, playerCarIdA = 1L, playerCarIdB = 2L)

        val result = AceWindowsMapper.mapVehicleApproach(buffer)

        assertEquals(sqrt(3.0), result.nearbyVehicles[0].distanceMeters, 0.0001)
    }
}
