package kurou.kodriver.data.mapper

import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsMapperTest {

    // オフセット定数（LmuWindowsMapper と一致）
    private companion object {
        const val TELEMETRY_BASE = 128464
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

        const val BUFFER_SIZE = 135_000

        fun emptyBuffer(playerIdx: Int = 0): ByteBuffer {
            val buf = ByteBuffer.allocate(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
            buf.put(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX, playerIdx.toByte())
            return buf
        }

        fun vehicleBase(playerIdx: Int = 0) =
            TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE
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

        assertEquals(45.5, result.fuel.currentLiters)
        assertEquals(100.0, result.fuel.capacityLiters)
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
    fun `ラップタイムフィールドは未実装のためゼロである`() {
        val result = LmuWindowsMapper.map(emptyBuffer())

        assertEquals(0L, result.timing.currentLapTimeMs)
        assertEquals(0L, result.timing.lastLapTimeMs)
        assertEquals(0L, result.timing.bestLapTimeMs)
        assertEquals(0L, result.timing.sector1Ms)
        assertEquals(0L, result.timing.sector2Ms)
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
            buf.putDouble(wb + OFF_WHEEL_BRAKE_TEMP, 200.0 + i * 5.0)
            buf.putDouble(wb + OFF_WHEEL_PRESSURE, 220.0 + i.toDouble())
            buf.putDouble(wb + OFF_WHEEL_WEAR, 0.9 - i * 0.05)
        }

        val result = LmuWindowsMapper.map(buf)

        assertEquals(4, result.tyres.wheels.size)
        WheelIndex.entries.forEachIndexed { i, wheel ->
            val tyre = result.tyres.wheels[wheel]!!
            assertEquals(350.0 + i * 10.0, tyre.surfaceTemperatureK, 1e-9)
            assertEquals(200.0 + i * 5.0, tyre.brakeTemperatureC, 1e-9)
            assertEquals(220.0 + i.toDouble(), tyre.pressureKpa, 1e-9)
            assertEquals(0.9 - i * 0.05, tyre.wear, 1e-9)
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
}
