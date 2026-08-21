package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.model.LmuWindowsFuelUnit
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LmuWindowsMapperTest {
    // オフセット定数（LmuWindowsMapper と一致）
    private companion object {
        const val SCORING_BASE = 1_632
        const val VEHICLE_SCORING_BASE = 2_192
        const val VEHICLE_SCORING_STRIDE = 584
        const val KELVIN_OFFSET = 273.15

        const val OFF_SCORING_CURRENT_ET = 68
        const val OFF_SCORING_NUM_VEHICLES = 104
        const val OFF_SCORING_BEST_LAP_TIME = 144
        const val OFF_SCORING_LAST_LAP_TIME = 168
        const val OFF_SCORING_IN_PITS = 198
        const val OFF_SCORING_IS_PLAYER = 196
        const val OFF_SCORING_VEHICLE_CLASS = 200
        const val OFF_SCORING_LAP_START_ET = 256
        const val OFF_SCORING_PIT_STATE = 457
        const val OFF_SCORING_IN_GARAGE_STALL = 507
        const val OFF_SCORING_BEST_LAP_SECTOR1 = 576
        const val OFF_SCORING_BEST_LAP_SECTOR2 = 580

        const val TELEMETRY_BASE = 128464
        const val OFF_ACTIVE_VEHICLES = 0
        const val OFF_PLAYER_VEHICLE_IDX = 1
        const val OFF_TELEM_INFO = 4
        const val VEHICLE_STRIDE = 1888

        const val OFF_LAP_NUMBER = 20
        const val OFF_POS_X = 160
        const val OFF_POS_Y = 168
        const val OFF_POS_Z = 176
        const val OFF_LOCAL_VEL_X = 184
        const val OFF_LOCAL_VEL_Y = 192
        const val OFF_LOCAL_VEL_Z = 200
        const val OFF_GEAR = 352
        const val OFF_ENGINE_RPM = 356
        const val OFF_ENGINE_MAX_RPM = 532
        const val OFF_UNFILTERED_THROTTLE = 388
        const val OFF_UNFILTERED_BRAKE = 396
        const val OFF_UNFILTERED_STEERING = 404
        const val OFF_UNFILTERED_CLUTCH = 412
        const val OFF_FUEL = 524
        const val OFF_FUEL_CAPACITY = 608
        const val OFF_WHEELS = 848
        const val WHEEL_STRIDE = 260
        const val OFF_WHEEL_BRAKE_TEMP = 24
        const val OFF_WHEEL_PRESSURE = 120
        const val OFF_WHEEL_TEMPERATURE_CENTER = 136 // +8 of OFF_WHEEL_TEMPERATURE(128)
        const val OFF_WHEEL_WEAR = 152
        const val OFF_WHEEL_TIRE_CARCASS_TEMPERATURE = 204

        const val BUFFER_SIZE = 135_000

        fun emptyBuffer(playerIdx: Int = 0): ByteBuffer {
            val buf = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            buf.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, playerIdx.toByte())
            return buf
        }

        fun vehicleBase(playerIdx: Int = 0) = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE

        fun vehicleScoringBase(index: Int = 0) = VEHICLE_SCORING_BASE + index * VEHICLE_SCORING_STRIDE
    }

    @Test
    fun `エンジンデータが正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        buf.putInt(vb + OFF_GEAR, 5)
        buf.putDouble(vb + OFF_ENGINE_RPM, 8_500.0)
        buf.putDouble(vb + OFF_ENGINE_MAX_RPM, 9_500.0)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(5, result.engine.gear)
        assertEquals(8_500.0, result.engine.rpm)
        assertEquals(9_500.0, result.engine.maxRpm)
    }

    @Test
    fun `入力データが正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        buf.putDouble(vb + OFF_UNFILTERED_THROTTLE, 0.75)
        buf.putDouble(vb + OFF_UNFILTERED_BRAKE, 0.1)
        buf.putDouble(vb + OFF_UNFILTERED_CLUTCH, 0.0)
        buf.putDouble(vb + OFF_UNFILTERED_STEERING, -0.3)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(0.75, result.inputs.throttle)
        assertEquals(0.1, result.inputs.brake)
        assertEquals(0.0, result.inputs.clutch)
        assertEquals(-0.3, result.inputs.steering)
    }

    @Test
    fun `燃料データが正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        buf.putDouble(vb + OFF_FUEL, 45.5)
        buf.putDouble(vb + OFF_FUEL_CAPACITY, 100.0)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(LmuWindowsFuelUnit(45.5), result.fuel.currentLiters)
        assertEquals(LmuWindowsFuelUnit(100.0), result.fuel.capacityLiters)
    }

    @Test
    fun `現在ラップ番号が正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        buf.putInt(vb + OFF_LAP_NUMBER, 7)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(7, result.timing.currentLap)
    }

    @Test
    fun `ラップタイムフィールドがScoringのプレイヤー車両から正しくパースされる`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 1)
        buf.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, 2)
        buf.putDouble(SCORING_BASE + OFF_SCORING_CURRENT_ET, 123.456)
        buf.put(scoringBase + OFF_SCORING_IS_PLAYER, 1)
        buf.putDouble(scoringBase + OFF_SCORING_LAP_START_ET, 100.000)
        buf.putDouble(scoringBase + OFF_SCORING_LAST_LAP_TIME, 92.345)
        buf.putDouble(scoringBase + OFF_SCORING_BEST_LAP_TIME, 91.234)
        buf.putFloat(scoringBase + OFF_SCORING_BEST_LAP_SECTOR1, 30.123f)
        buf.putFloat(scoringBase + OFF_SCORING_BEST_LAP_SECTOR2, 60.456f)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(23_456L, result.timing.currentLapTimeMs)
        assertEquals(92_345L, result.timing.lastLapTimeMs)
        assertEquals(91_234L, result.timing.bestLapTimeMs)
        assertEquals(30_123L, result.timing.sector1Ms)
        assertEquals(60_456L, result.timing.sector1And2Ms)
    }

    @Test
    fun `Scoringのプレイヤー車両が見つからない場合はラップタイムフィールドがゼロである`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)
        buf.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, 1)
        buf.putDouble(SCORING_BASE + OFF_SCORING_CURRENT_ET, 123.456)
        buf.put(scoringBase + OFF_SCORING_IS_PLAYER, 0)
        buf.putDouble(scoringBase + OFF_SCORING_BEST_LAP_TIME, 91.234)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(0L, result.timing.currentLapTimeMs)
        assertEquals(0L, result.timing.lastLapTimeMs)
        assertEquals(0L, result.timing.bestLapTimeMs)
        assertEquals(0L, result.timing.sector1Ms)
        assertEquals(0L, result.timing.sector1And2Ms)
    }

    @Test
    fun `Scoringの104台を超える車両は探索しない`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 127)
        buf.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, 128)
        buf.put(scoringBase + OFF_SCORING_IS_PLAYER, 1)
        buf.putDouble(scoringBase + OFF_SCORING_BEST_LAP_TIME, 88.765)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(0L, result.timing.bestLapTimeMs)
    }

    @Test
    fun `車両位置が正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        buf.putDouble(vb + OFF_POS_X, 100.0)
        buf.putDouble(vb + OFF_POS_Y, 200.0)
        buf.putDouble(vb + OFF_POS_Z, 300.0)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(100.0, result.vehicle.positionX)
        assertEquals(200.0, result.vehicle.positionY)
        assertEquals(300.0, result.vehicle.positionZ)
    }

    @Test
    fun `速度がローカル速度ベクトルから計算される`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        // 3-4-0 ベクトル → speed = 5.0 m/s → 18.0 km/h
        buf.putDouble(vb + OFF_LOCAL_VEL_X, 3.0)
        buf.putDouble(vb + OFF_LOCAL_VEL_Y, 4.0)
        buf.putDouble(vb + OFF_LOCAL_VEL_Z, 0.0)

        val result = LmuWindowsMapper.map(buf)

        assertEquals(5.0, result.vehicle.speedMs, 1e-9)
        assertEquals(18.0, result.vehicle.speedKmh, 1e-9)
    }

    @Test
    fun `タイヤデータが全ホイール分正しくパースされる`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()

        WheelIndex.entries.forEachIndexed { i, wheel ->
            val wb = vb + OFF_WHEELS + i * WHEEL_STRIDE
            buf.putDouble(wb + OFF_WHEEL_TEMPERATURE_CENTER, 350.0 + i * 10.0)
            buf.putDouble(wb + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE, 345.0 + i * 10.0)
            buf.putDouble(wb + OFF_WHEEL_BRAKE_TEMP, 200.0 + i * 5.0)
            buf.putDouble(wb + OFF_WHEEL_PRESSURE, 220.0 + i.toDouble())
            buf.putDouble(wb + OFF_WHEEL_WEAR, 0.9 - i * 0.05)
        }

        val result = LmuWindowsMapper.map(buf)

        assertEquals(4, result.tyres.wheels.size)
        WheelIndex.entries.forEachIndexed { i, wheel ->
            val tyre = requireNotNull(result.tyres.wheels[wheel])
            assertEquals((350.0 + i * 10.0 - KELVIN_OFFSET).toFloat(), tyre.surfaceTemperature.value, 1e-4f)
            assertEquals((345.0 + i * 10.0 - KELVIN_OFFSET).toFloat(), tyre.carcassTemperature.value, 1e-4f)
            assertEquals((200.0 + i * 5.0).toFloat(), tyre.brakeTemperature.value, 1e-4f)
            assertEquals(220.0 + i.toDouble(), tyre.pressureKpa.value, 1e-9)
            assertEquals(0.9 - i * 0.05, tyre.wear.value, 1e-9)
        }
    }

    @Test
    fun `playerIndexに応じてvehicleBaseがずれる`() {
        // playerIdx=1 の車両データが playerIdx=0 のスロットには書かれていないことを確認
        val vb0 = vehicleBase(playerIdx = 0)
        val vb1 = vehicleBase(playerIdx = 1)
        val buf = emptyBuffer(playerIdx = 1)

        buf.putInt(vb0 + OFF_GEAR, 1) // playerIdx=0 スロット（読まれないはず）
        buf.putInt(vb1 + OFF_GEAR, 6) // playerIdx=1 スロット（読まれるはず）

        val result = LmuWindowsMapper.map(buf)

        assertEquals(6, result.engine.gear)
    }

    @Test
    fun `タイヤマップに全WheelIndexが含まれる`() {
        val result = LmuWindowsMapper.map(emptyBuffer())

        assertTrue(WheelIndex.FRONT_LEFT in result.tyres.wheels)
        assertTrue(WheelIndex.FRONT_RIGHT in result.tyres.wheels)
        assertTrue(WheelIndex.REAR_LEFT in result.tyres.wheels)
        assertTrue(WheelIndex.REAR_RIGHT in result.tyres.wheels)
    }

    @Test
    fun `findPlayerVehicleBaseはplayerIndexに応じたオフセットを返す`() {
        val buf = emptyBuffer(playerIdx = 1)
        buf.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, 2)

        val vehicleBase = LmuWindowsMapper.findPlayerVehicleBase(buf)

        assertEquals(vehicleBase(playerIdx = 1), vehicleBase)
    }

    @Test
    fun `findPlayerVehicleBaseはactiveVehiclesが0のときnullを返す`() {
        val buf = emptyBuffer(playerIdx = 0)
        buf.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, 0)

        assertEquals(null, LmuWindowsMapper.findPlayerVehicleBase(buf))
    }

    @Test
    fun `findPlayerVehicleBaseはplayerIdxがactiveVehicles以上のときnullを返す`() {
        val buf = emptyBuffer(playerIdx = 1)
        buf.put(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES, 1)

        assertEquals(null, LmuWindowsMapper.findPlayerVehicleBase(buf))
    }

    @Test
    fun `maxVehicleCountはバッファサイズと1台あたりのヘッダーサイズから車両数上限を算出する`() {
        val headerSizePerVehicle = 8
        val headerSize = vehicleBase(playerIdx = 0) + headerSizePerVehicle
        val buf = ByteBuffer.allocate(headerSize + VEHICLE_STRIDE * 3).order(ByteOrder.LITTLE_ENDIAN)

        val result = LmuWindowsMapper.maxVehicleCount(buf, headerSizePerVehicle)

        assertEquals(3, result)
    }

    @Test
    fun `maxVehicleCountはバッファがヘッダーサイズに満たない場合0を返す`() {
        val result = LmuWindowsMapper.maxVehicleCount(ByteBuffer.allocate(1), headerSizePerVehicle = 8)

        assertEquals(0, result)
    }

    @Test
    fun `readCarcassTemperaturesKは4輪ぶんのカーカス温度をKelvinで返す`() {
        val vb = vehicleBase()
        val buf = emptyBuffer()
        WheelIndex.entries.forEachIndexed { i, _ ->
            val wb = vb + OFF_WHEELS + i * WHEEL_STRIDE
            buf.putDouble(wb + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE, 345.0 + i * 10.0)
        }

        val result = LmuWindowsMapper.readCarcassTemperaturesK(buf, vb)

        WheelIndex.entries.forEachIndexed { i, wheel ->
            assertEquals(345.0 + i * 10.0, requireNotNull(result[wheel]), 1e-9)
        }
    }

    @Test
    fun `findPlayerVehicleScoringBaseはmIsPlayerが立っている車両のオフセットを返す`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 1)
        buf.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, 2)
        buf.put(scoringBase + OFF_SCORING_IS_PLAYER, 1)

        assertEquals(scoringBase, LmuWindowsMapper.findPlayerVehicleScoringBase(buf))
    }

    @Test
    fun `findPlayerVehicleScoringBaseはmIsPlayerな車両が存在しない場合nullを返す`() {
        val buf = emptyBuffer()
        buf.putInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES, 2)

        assertEquals(null, LmuWindowsMapper.findPlayerVehicleScoringBase(buf))
    }

    @Test
    fun `readVehicleClassNameはmVehicleClassをnull終端でトリムした文字列で返す`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)
        "Hypercar".toByteArray(Charsets.US_ASCII).forEachIndexed { i, byte ->
            buf.put(scoringBase + OFF_SCORING_VEHICLE_CLASS + i, byte)
        }

        val result = LmuWindowsMapper.readVehicleClassName(buf, scoringBase)

        assertEquals("Hypercar", result)
    }

    @Test
    fun `readVehicleClassNameはnullバイトのみの場合は空文字列を返す`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)

        val result = LmuWindowsMapper.readVehicleClassName(buf, scoringBase)

        assertEquals("", result)
    }

    @Test
    fun `readPitStatusはmInPits_mPitState_mInGarageStallを正しくパースする`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)
        buf.put(scoringBase + OFF_SCORING_IN_PITS, 1)
        buf.put(scoringBase + OFF_SCORING_PIT_STATE, 2)
        buf.put(scoringBase + OFF_SCORING_IN_GARAGE_STALL, 1)

        val result = LmuWindowsMapper.readPitStatus(buf, scoringBase)

        assertTrue(result.inPits)
        assertEquals(LmuWindowsPitState.ENTERING, result.pitState)
        assertTrue(result.inGarageStall)
    }

    @Test
    fun `readPitStatusは全てのmPitStateの値を取得できる`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)
        val expected =
            mapOf(
                0 to LmuWindowsPitState.NONE,
                1 to LmuWindowsPitState.REQUESTED,
                2 to LmuWindowsPitState.ENTERING,
                3 to LmuWindowsPitState.STOPPED,
                4 to LmuWindowsPitState.EXITING,
            )

        expected.forEach { (rawValue, pitState) ->
            buf.put(scoringBase + OFF_SCORING_PIT_STATE, rawValue.toByte())

            val result = LmuWindowsMapper.readPitStatus(buf, scoringBase)

            assertEquals(pitState, result.pitState, "rawValue=$rawValue")
        }
    }

    @Test
    fun `readPitStatusはmPitStateが未知の値のときUNKNOWNを返す`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)
        buf.put(scoringBase + OFF_SCORING_PIT_STATE, 99)

        val result = LmuWindowsMapper.readPitStatus(buf, scoringBase)

        assertEquals(LmuWindowsPitState.UNKNOWN, result.pitState)
    }

    @Test
    fun `readPitStatusはbool系フィールドが0のときfalseを返す`() {
        val buf = emptyBuffer()
        val scoringBase = vehicleScoringBase(index = 0)

        val result = LmuWindowsMapper.readPitStatus(buf, scoringBase)

        assertFalse(result.inPits)
        assertFalse(result.inGarageStall)
    }
}
