package kurou.kodriver.core.lmuwindowsdata.mapper

import kurou.kodriver.domain.model.LmuWindowsEngineData
import kurou.kodriver.domain.model.LmuWindowsFuelData
import kurou.kodriver.domain.model.LmuWindowsInputsData
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsTimingData
import kurou.kodriver.domain.model.LmuWindowsTyreData
import kurou.kodriver.domain.model.LmuWindowsTyreWheelData
import kurou.kodriver.domain.model.LmuWindowsVehicleData
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import kotlin.math.roundToLong

/**
 * LMU 共有メモリ (LMU_WINDOWS_Data) の ByteBuffer を LmuWindowsTelemetryData に変換する。
 *
 * オフセットは LMU 共有メモリの Python ctypes 移植である
 * lmu_data.py の構造体レイアウト (_pack_=4) から算出。
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
 *   mNumVehicles (c_int)       : +104
 *
 * rF2VehicleScoring オフセット:
 *   mVehicles[104]             : 先頭オフセット 2192, stride=584
 *   mInPits                    : +198 (bool、ピットレーン走行中。リモート車両は不正確な場合あり)
 *   mBestLapTime               : +144
 *   mLastLapTime               : +168
 *   mIsPlayer                  : +196
 *   mVehicleClass (char[32])   : +200 （人間可読なクラス名。2026年8月時点の実測値: "GT3", "GTE", "LMP3", "LMP2", "LMP2_ELMS", "Hyper"）
 *   mLapStartET                : +256
 *   mPitState                  : +457 (uint8、0=なし,1=要求,2=入場中,3=停止中,4=出場中)
 *   mInGarageStall              : +507 (bool、正しいガレージストール内にいるか)
 *   mBestLapSector1            : +576
 *   mBestLapSector2            : +580
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
 *   mVirtualEnergy       : +776 (float, LMU固有。0.0-1.0の残量割合)
 *
 * LMUWheel 主要フィールドオフセット (ホイール先頭からの相対値):
 *   mBrakeTemp           : +24
 *   mPressure            : +120
 *   mTemperature[3]      : +128 (Kelvin, 中央値=+136)
 *   mWear                : +152
 *   mTireCarcassTemperature : +204 (Kelvin)
 */
internal object LmuWindowsMapper {
    private const val SCORING_BASE = 1632
    private const val VEHICLE_SCORING_BASE = 2192
    private const val VEHICLE_SCORING_STRIDE = 584
    private const val MAX_SCORING_VEHICLES = 104

    private const val OFF_SCORING_CURRENT_ET = 68
    private const val OFF_SCORING_SESSION = 64
    private const val OFF_SCORING_MAX_LAPS = 84
    private const val OFF_SCORING_NUM_VEHICLES = 104

    private const val OFF_SCORING_IN_PITS = 198
    private const val OFF_SCORING_BEST_LAP_TIME = 144
    private const val OFF_SCORING_LAST_LAP_TIME = 168
    private const val OFF_SCORING_IS_PLAYER = 196
    private const val OFF_SCORING_VEHICLE_CLASS = 200
    private const val VEHICLE_CLASS_LENGTH = 32
    private const val OFF_SCORING_LAP_START_ET = 256
    private const val OFF_SCORING_PIT_STATE = 457
    private const val OFF_SCORING_IN_GARAGE_STALL = 507
    private const val OFF_SCORING_BEST_LAP_SECTOR1 = 576
    private const val OFF_SCORING_BEST_LAP_SECTOR2 = 580

    private const val TELEMETRY_BASE = 128464

    private const val OFF_ACTIVE_VEHICLES = 0
    private const val OFF_PLAYER_VEHICLE_IDX = 1
    private const val OFF_TELEM_INFO = 4
    internal const val VEHICLE_STRIDE = 1888

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
    private const val OFF_VIRTUAL_ENERGY = 776

    private const val WHEEL_STRIDE = 260
    private const val OFF_WHEEL_BRAKE_TEMP = 24
    private const val OFF_WHEEL_PRESSURE = 120
    private const val OFF_WHEEL_TEMPERATURE = 128
    private const val OFF_WHEEL_WEAR = 152
    private const val OFF_WHEEL_TIRE_CARCASS_TEMPERATURE = 204

    fun map(buffer: ByteBuffer): LmuWindowsTelemetryData {
        val vehicleBase = vehicleTelemetryBase(readPlayerVehicleIdx(buffer))
        val vehicleScoringBase = findPlayerVehicleScoringBase(buffer)

        return LmuWindowsTelemetryData(
            timestampMs = System.currentTimeMillis(),
            engine =
                LmuWindowsEngineData(
                    rpm = buffer.getDouble(vehicleBase + OFF_ENGINE_RPM),
                    maxRpm = buffer.getDouble(vehicleBase + OFF_ENGINE_MAX_RPM),
                    gear = buffer.getInt(vehicleBase + OFF_GEAR),
                ),
            inputs =
                LmuWindowsInputsData(
                    throttle = buffer.getDouble(vehicleBase + OFF_UNFILTERED_THROTTLE),
                    brake = buffer.getDouble(vehicleBase + OFF_UNFILTERED_BRAKE),
                    clutch = buffer.getDouble(vehicleBase + OFF_UNFILTERED_CLUTCH),
                    steering = buffer.getDouble(vehicleBase + OFF_UNFILTERED_STEERING),
                ),
            tyres = mapTyres(buffer, vehicleBase),
            fuel =
                LmuWindowsFuelData(
                    currentLiters = buffer.getDouble(vehicleBase + OFF_FUEL),
                    capacityLiters = buffer.getDouble(vehicleBase + OFF_FUEL_CAPACITY),
                ),
            timing =
                LmuWindowsTimingData(
                    currentLapTimeMs = currentLapTimeMs(buffer, vehicleScoringBase),
                    lastLapTimeMs = vehicleScoringBase.readDoubleSecondsAsMillis(buffer, OFF_SCORING_LAST_LAP_TIME),
                    bestLapTimeMs = vehicleScoringBase.readDoubleSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_TIME),
                    sector1Ms = vehicleScoringBase.readFloatSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_SECTOR1),
                    sector1And2Ms = vehicleScoringBase.readFloatSecondsAsMillis(buffer, OFF_SCORING_BEST_LAP_SECTOR2),
                    currentLap = buffer.getInt(vehicleBase + OFF_LAP_NUMBER),
                    maxLaps = buffer.getInt(SCORING_BASE + OFF_SCORING_MAX_LAPS),
                ),
            vehicle =
                LmuWindowsVehicleData(
                    localVelocityX = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_X),
                    localVelocityY = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_Y),
                    localVelocityZ = buffer.getDouble(vehicleBase + OFF_LOCAL_VEL_Z),
                    positionX = buffer.getDouble(vehicleBase + OFF_POS_X),
                    positionY = buffer.getDouble(vehicleBase + OFF_POS_Y),
                    positionZ = buffer.getDouble(vehicleBase + OFF_POS_Z),
                ),
        )
    }

    /**
     * activeVehicles / playerVehicleIdx を検証したうえで、プレイヤー車両の telemInfo 先頭オフセットを返す。
     * 車両が存在しない場合は null を返す。
     */
    internal fun findPlayerVehicleBase(buffer: ByteBuffer): Int? {
        val activeVehicles = readActiveVehicleCount(buffer)
        val playerIdx = readPlayerVehicleIdx(buffer)
        if (activeVehicles == 0 || playerIdx >= activeVehicles) return null
        return vehicleTelemetryBase(playerIdx)
    }

    /** telemetry セグメントの activeVehicles (uint8) を返す。 */
    internal fun readActiveVehicleCount(buffer: ByteBuffer): Int =
        buffer.get(TELEMETRY_BASE + OFF_ACTIVE_VEHICLES).toInt() and 0xFF

    /** telemetry セグメントの playerVehicleIdx (uint8) を返す。 */
    internal fun readPlayerVehicleIdx(buffer: ByteBuffer): Int =
        buffer.get(TELEMETRY_BASE + OFF_PLAYER_VEHICLE_IDX).toInt() and 0xFF

    /** 指定した車両インデックスの telemInfo 先頭オフセットを返す（範囲チェックなし）。 */
    internal fun vehicleTelemetryBase(index: Int): Int = TELEMETRY_BASE + OFF_TELEM_INFO + index * VEHICLE_STRIDE

    /** プレイヤー車両のバーチャルエナジー残量割合 (0.0-1.0) を返す。 */
    internal fun readVirtualEnergyRatio(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): Double = buffer.getFloat(vehicleBase + OFF_VIRTUAL_ENERGY).toDouble()

    /** Scoring の mSession (プラクティス・予選・レースなどのセッション種別) を返す。 */
    internal fun readSession(buffer: ByteBuffer): Int = buffer.getInt(SCORING_BASE + OFF_SCORING_SESSION)

    /** プレイヤー車両の4輪ぶんのカーカス温度 (Kelvin) を返す。 */
    internal fun readCarcassTemperaturesK(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): Map<WheelIndex, Double> =
        WheelIndex.entries.associateWith { wheel ->
            val offset = vehicleBase + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
            buffer.getDouble(offset + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE)
        }

    /** プレイヤー車両の4輪ぶんの残タイヤ溝割合 (0.0-1.0) を返す。 */
    internal fun readWearFractions(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): Map<WheelIndex, Double> =
        WheelIndex.entries.associateWith { wheel ->
            val offset = vehicleBase + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
            buffer.getDouble(offset + OFF_WHEEL_WEAR)
        }

    /** Scoring セグメントの mVehicleClass (char[32]) をトリムした文字列として返す。 */
    internal fun readVehicleClassName(
        buffer: ByteBuffer,
        vehicleScoringBase: Int,
    ): String {
        val bytes = ByteArray(VEHICLE_CLASS_LENGTH)
        for (i in 0 until VEHICLE_CLASS_LENGTH) {
            bytes[i] = buffer.get(vehicleScoringBase + OFF_SCORING_VEHICLE_CLASS + i)
        }
        val nullIndex = bytes.indexOf(0).let { if (it < 0) bytes.size else it }
        return String(bytes, 0, nullIndex, Charsets.US_ASCII)
    }

    /** Scoring セグメントのプレイヤー車両のピット状態 (mInPits / mPitState / mInGarageStall) を返す。 */
    internal fun readPitStatus(
        buffer: ByteBuffer,
        vehicleScoringBase: Int,
    ): LmuWindowsPitStatusData =
        LmuWindowsPitStatusData(
            inPits = buffer.get(vehicleScoringBase + OFF_SCORING_IN_PITS).toInt() != 0,
            pitState =
                LmuWindowsPitState.fromRaw(buffer.get(vehicleScoringBase + OFF_SCORING_PIT_STATE).toInt() and 0xFF),
            inGarageStall = buffer.get(vehicleScoringBase + OFF_SCORING_IN_GARAGE_STALL).toInt() != 0,
        )

    internal fun findPlayerVehicleScoringBase(buffer: ByteBuffer): Int? {
        val vehicleCount = buffer.getInt(SCORING_BASE + OFF_SCORING_NUM_VEHICLES).coerceIn(0, MAX_SCORING_VEHICLES)
        for (index in 0 until vehicleCount) {
            val vehicleBase = VEHICLE_SCORING_BASE + index * VEHICLE_SCORING_STRIDE
            if (buffer.get(vehicleBase + OFF_SCORING_IS_PLAYER).toInt() != 0) {
                return vehicleBase
            }
        }
        return null
    }

    private fun currentLapTimeMs(
        buffer: ByteBuffer,
        vehicleScoringBase: Int?,
    ): Long {
        if (vehicleScoringBase == null) return 0L
        val currentSessionTime = buffer.getDouble(SCORING_BASE + OFF_SCORING_CURRENT_ET)
        val lapStartSessionTime = buffer.getDouble(vehicleScoringBase + OFF_SCORING_LAP_START_ET)
        return secondsToMillis(currentSessionTime - lapStartSessionTime)
    }

    private fun Int?.readDoubleSecondsAsMillis(
        buffer: ByteBuffer,
        offset: Int,
    ): Long = if (this == null) 0L else secondsToMillis(buffer.getDouble(this + offset))

    private fun Int?.readFloatSecondsAsMillis(
        buffer: ByteBuffer,
        offset: Int,
    ): Long = if (this == null) 0L else secondsToMillis(buffer.getFloat(this + offset).toDouble())

    private fun secondsToMillis(seconds: Double): Long =
        if (seconds.isFinite() && seconds > 0.0) {
            (seconds * MILLIS_PER_SECOND).roundToLong()
        } else {
            0L
        }

    private fun mapTyres(
        buffer: ByteBuffer,
        vehicleBase: Int,
    ): LmuWindowsTyreData {
        val wheels =
            WheelIndex.entries.associateWith { wheel ->
                val offset = vehicleBase + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
                val surfaceTempK = buffer.getDouble(offset + OFF_WHEEL_TEMPERATURE + 8)
                LmuWindowsTyreWheelData(
                    surfaceTemperatureK = surfaceTempK,
                    carcassTemperatureK = buffer.getDouble(offset + OFF_WHEEL_TIRE_CARCASS_TEMPERATURE),
                    brakeTemperatureC = buffer.getDouble(offset + OFF_WHEEL_BRAKE_TEMP),
                    pressureKpa = buffer.getDouble(offset + OFF_WHEEL_PRESSURE),
                    wear = buffer.getDouble(offset + OFF_WHEEL_WEAR),
                )
            }
        return LmuWindowsTyreData(wheels)
    }

    private const val MILLIS_PER_SECOND = 1_000
}
