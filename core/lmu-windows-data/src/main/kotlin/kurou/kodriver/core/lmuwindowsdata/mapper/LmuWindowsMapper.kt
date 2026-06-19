package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.model.EngineData
import kurou.kodriver.domain.model.FuelData
import kurou.kodriver.domain.model.InputsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.TimingData
import kurou.kodriver.domain.model.TyreData
import kurou.kodriver.domain.model.TyreWheelData
import kurou.kodriver.domain.model.VehicleData
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer

/**
 * LMU 共有メモリ (LMU_WINDOWS_Data) の ByteBuffer を LmuWindowsTelemetryData に変換する。
 *
 * オフセットは pyLMUSharedMemory (TinyPedal/pyLMUSharedMemory) の
 * lmu_data.py に定義された ctypes 構造体レイアウト (_pack_=4) から算出。
 *
 * LMUObjectOut レイアウト:
 *   generic  (LMUGeneric)      : 332 bytes  → 先頭オフセット 0
 *   paths    (LMUPathData)     : 1300 bytes → 先頭オフセット 332
 *   scoring  (LMUScoringData)  : 126832 bytes → 先頭オフセット 1632
 *   telemetry (LMUTelemetryData): 196356 bytes → 先頭オフセット 128464
 *
 * LMUScoringInfo オフセット (1632 から, _pack_=4):
 *   mTrackName (c_char*64)     : +0
 *   mSession   (c_int)         : +64
 *   mCurrentET (c_double)      : +68
 *   mEndET     (c_double)      : +76
 *   mMaxLaps   (c_int)         : +84
 *
 * LMUTelemetryData オフセット (128464 から):
 *   [+0] activeVehicles (uint8)
 *   [+1] playerVehicleIdx (uint8)  ← プレイヤー車両インデックス
 *   [+2] playerHasVehicle (bool)
 *   [+4] telemInfo[104] (LMUVehicleTelemetry, stride=1888)
 *
 * LMUVehicleTelemetry 主要フィールドオフセット (車両先頭からの相対値):
 *   mLapNumber           : +20
 *   mPos (Vec3)          : +160 (x), +168 (y), +176 (z)
 *   mLocalVel (Vec3)     : +184 (x), +192 (y), +200 (z)
 *   mGear                : +352
 *   mEngineRPM           : +356
 *   mUnfilteredThrottle  : +388
 *   mUnfilteredBrake     : +396
 *   mUnfilteredSteering  : +404
 *   mUnfilteredClutch    : +412
 *   mFuel                : +524
 *   mEngineMaxRPM        : +532
 *   mFuelCapacity        : +608
 *   mWheels[4]           : +848 (LMUWheel, stride=260)
 *
 * LMUWheel 主要フィールドオフセット (ホイール先頭からの相対値):
 *   mBrakeTemp           : +24
 *   mPressure            : +120
 *   mTemperature[3]      : +128 (Kelvin, 中央値=+136)
 *   mWear                : +152
 */
internal object LmuWindowsMapper {

    private const val SCORING_BASE = 1632
    private const val OFF_SCORING_MAX_LAPS = 84

    private const val TELEMETRY_BASE = 128464

    private const val OFF_PLAYER_VEHICLE_IDX = 1
    private const val OFF_TELEM_INFO = 4
    private const val VEHICLE_STRIDE = 1888

    private const val OFF_LAP_NUMBER = 20
    private const val OFF_POS_X = 160
    private const val OFF_POS_Y = 168
    private const val OFF_POS_Z = 176
    private const val OFF_LOCAL_VEL_X = 184
    private const val OFF_LOCAL_VEL_Y = 192
    private const val OFF_LOCAL_VEL_Z = 200
    private const val OFF_GEAR = 352
    private const val OFF_ENGINE_RPM = 356
    private const val OFF_ENGINE_MAX_RPM = 532
    private const val OFF_UNFILTERED_THROTTLE = 388
    private const val OFF_UNFILTERED_BRAKE = 396
    private const val OFF_UNFILTERED_STEERING = 404
    private const val OFF_UNFILTERED_CLUTCH = 412
    private const val OFF_FUEL = 524
    private const val OFF_FUEL_CAPACITY = 608
    private const val OFF_WHEELS = 848

    private const val WHEEL_STRIDE = 260
    private const val OFF_WHEEL_BRAKE_TEMP = 24
    private const val OFF_WHEEL_PRESSURE = 120
    private const val OFF_WHEEL_TEMPERATURE = 128
    private const val OFF_WHEEL_WEAR = 152

    fun map(buffer: ByteBuffer): LmuWindowsTelemetryData {
        val playerIdx = buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF
        val vehicleBase = TELEMETRY_BASE + OFF_TELEM_INFO + playerIdx * VEHICLE_STRIDE

        return LmuWindowsTelemetryData(
            timestampMs = System.currentTimeMillis(),
            engine = EngineData(
                rpm = buffer.getDouble(vehicleBase + OFF_ENGINE_RPM),
                maxRpm = buffer.getDouble(vehicleBase + OFF_ENGINE_MAX_RPM),
                gear = buffer.getInt(vehicleBase + OFF_GEAR),
            ),
            inputs = InputsData(
                throttle = buffer.getDouble(vehicleBase + OFF_UNFILTERED_THROTTLE),
                brake = buffer.getDouble(vehicleBase + OFF_UNFILTERED_BRAKE),
                clutch = buffer.getDouble(vehicleBase + OFF_UNFILTERED_CLUTCH),
                steering = buffer.getDouble(vehicleBase + OFF_UNFILTERED_STEERING),
            ),
            tyres = mapTyres(buffer, vehicleBase),
            fuel = FuelData(
                currentLiters = buffer.getDouble(vehicleBase + OFF_FUEL),
                capacityLiters = buffer.getDouble(vehicleBase + OFF_FUEL_CAPACITY),
            ),
            timing = TimingData(
                currentLapTimeMs = 0L,
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
                sector1Ms = 0L,
                sector2Ms = 0L,
                currentLap = buffer.getInt(vehicleBase + OFF_LAP_NUMBER),
                maxLaps = buffer.getInt(SCORING_BASE + OFF_SCORING_MAX_LAPS),
            ),
            vehicle = VehicleData(
                localVelocityX = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_X),
                localVelocityY = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_Y),
                localVelocityZ = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_Z),
                positionX = buffer.getDouble(vehicleBase + OFF_POS_X),
                positionY = buffer.getDouble(vehicleBase + OFF_POS_Y),
                positionZ = buffer.getDouble(vehicleBase + OFF_POS_Z),
            ),
        )
    }

    private fun mapTyres(buffer: ByteBuffer, vehicleBase: Int): TyreData {
        val wheels = WheelIndex.entries.associateWith { wheel ->
            val offset = vehicleBase + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
            val surfaceTempK = buffer.getDouble(offset + OFF_WHEEL_TEMPERATURE + 8)
            TyreWheelData(
                surfaceTemperatureK = surfaceTempK,
                brakeTemperatureC = buffer.getDouble(offset + OFF_WHEEL_BRAKE_TEMP),
                pressureKpa = buffer.getDouble(offset + OFF_WHEEL_PRESSURE),
                wear = buffer.getDouble(offset + OFF_WHEEL_WEAR),
            )
        }
        return TyreData(wheels)
    }
}
