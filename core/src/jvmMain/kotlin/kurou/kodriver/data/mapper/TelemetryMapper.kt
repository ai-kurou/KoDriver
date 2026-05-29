package kurou.kodriver.data.mapper

import kurou.kodriver.domain.model.*
import java.nio.ByteBuffer

/**
 * rF2 共有メモリ (rFactor2SMMP_Telemetry) の ByteBuffer を TelemetryData に変換する。
 *
 * オフセットは rF2SharedMemoryMap.hpp (TheIronWolfModding/rF2SharedMemoryMapPlugin) の
 * C++ 構造体レイアウト（MSVC デフォルトアライメント）から算出。
 *
 * 構造体の概略:
 *   [0]  rF2MappedBufferVersionBlock (8 bytes: 2x uint32)
 *   [8]  rF2VehicleTelemetry mVehicles[128] の先頭
 *
 * rF2VehicleTelemetry のフィールドオフセット（vehicle先頭からの相対値）:
 *   int    mID               : +0     (4 bytes)
 *   double mDeltaTime        : +8     (padding 4 bytes after mID)
 *   double mElapsedTime      : +16
 *   int    mLapNumber        : +24    (4 bytes)
 *   [padding 4 bytes]
 *   double mLapStartET       : +32
 *   char   mVehicleName[64]  : +40
 *   char   mTrackName[64]    : +104
 *   rF2Vec3 mPos             : +168   (3x double = 24 bytes)
 *   rF2Vec3 mLocalVel        : +192
 *   rF2Vec3 mLocalAccel      : +216
 *   rF2Vec3 mOri[3]          : +240   (9x double = 72 bytes)
 *   rF2Vec3 mLocalRot        : +312
 *   rF2Vec3 mLocalRotAccel   : +336
 *   int    mGear             : +360
 *   [padding 4 bytes]
 *   double mEngineRPM        : +368
 *   double mEngineWaterTemp  : +376
 *   double mEngineOilTemp    : +384
 *   double mClutchRPM        : +392
 *   double mUnfilteredThrottle: +400
 *   double mUnfilteredBrake  : +408
 *   double mUnfilteredSteering: +416
 *   double mUnfilteredClutch : +424
 *   double mFilteredThrottle : +432
 *   double mFilteredBrake    : +440
 *   double mFilteredSteering : +448
 *   double mFilteredClutch   : +456
 *   ...
 *   double mFuel             : +536
 *   double mEngineMaxRPM     : +544
 *   ...
 *   double mFuelCapacity     : +624
 *   ...
 *   rF2Wheel mWheels[4]      : +712  (各ホイール = 120 bytes)
 *
 * rF2Wheel のフィールドオフセット（ホイール先頭からの相対値）:
 *   double mSuspensionDeflection: +0
 *   double mRideHeight          : +8
 *   double mSuspForce           : +16
 *   double mBrakeTemp           : +24   (Celsius)
 *   double mBrakePressure       : +32
 *   double mRotation            : +40
 *   double mLateralPatchVel     : +48
 *   double mLongitudinalPatchVel: +56
 *   double mLateralGroundVel    : +64
 *   double mLongitudinalGroundVel: +72
 *   double mCamber              : +80
 *   double mLateralForce        : +88
 *   double mLongitudinalForce   : +96
 *   double mTireLoad            : +104
 *   double mGripFract           : +112
 *   double mPressure            : +120  (kPa)
 *   double mTemperature[3]      : +128  (Kelvin: 外側,中央,内側)
 *   double mWear                : +152  (0.0=新品, 1.0=完全摩耗)
 *   char   mTerrainName[16]     : +160
 *   byte   mSurfaceType         : +176
 *   bool   mFlat                : +177
 *   bool   mDetached            : +178
 *   byte   mStaticUndeflectedRadius: +179
 *   double mVerticalTireDeflection: +184 (padding 4 bytes at 180)
 *   double mWheelYLocation      : +192
 *   double mToe                 : +200
 *   double mTireCarcassTemperature: +208
 *   double mTireInnerLayerTemperature[3]: +216
 *   [total wheel size = 240 bytes]
 *
 * ※ これらのオフセットは rF2SharedMemoryMap.hpp を参照して検証してください。
 */
internal object TelemetryMapper {

    // rF2MappedBufferVersionBlock のサイズ（uint32 x2）
    private const val HEADER_SIZE = 8

    // rF2VehicleTelemetry フィールドオフセット（vehicleBase からの相対値）
    private const val OFF_GEAR = 360
    private const val OFF_ENGINE_RPM = 368
    private const val OFF_ENGINE_MAX_RPM = 544
    private const val OFF_UNFILTERED_THROTTLE = 400
    private const val OFF_UNFILTERED_BRAKE = 408
    private const val OFF_UNFILTERED_STEERING = 416
    private const val OFF_UNFILTERED_CLUTCH = 424
    private const val OFF_FUEL = 536
    private const val OFF_FUEL_CAPACITY = 624
    private const val OFF_LOCAL_VEL_X = 192
    private const val OFF_LOCAL_VEL_Y = 200
    private const val OFF_LOCAL_VEL_Z = 208
    private const val OFF_POS_X = 168
    private const val OFF_POS_Y = 176
    private const val OFF_POS_Z = 184
    private const val OFF_LAP_NUMBER = 24
    private const val OFF_WHEELS = 712

    // rF2Wheel 1ホイールあたりのサイズ
    private const val WHEEL_STRIDE = 240

    // rF2Wheel フィールドオフセット（ホイール先頭からの相対値）
    private const val OFF_WHEEL_BRAKE_TEMP = 24
    private const val OFF_WHEEL_PRESSURE = 120
    private const val OFF_WHEEL_TEMPERATURE = 128  // double[3]: 外側,中央,内側 の平均を使用
    private const val OFF_WHEEL_WEAR = 152

    fun map(buffer: ByteBuffer): TelemetryData {
        val base = HEADER_SIZE

        return TelemetryData(
            timestampMs = System.currentTimeMillis(),
            engine = EngineData(
                rpm = buffer.getDouble(base + OFF_ENGINE_RPM),
                maxRpm = buffer.getDouble(base + OFF_ENGINE_MAX_RPM),
                gear = buffer.getInt(base + OFF_GEAR),
            ),
            inputs = InputsData(
                throttle = buffer.getDouble(base + OFF_UNFILTERED_THROTTLE),
                brake = buffer.getDouble(base + OFF_UNFILTERED_BRAKE),
                clutch = buffer.getDouble(base + OFF_UNFILTERED_CLUTCH),
                steering = buffer.getDouble(base + OFF_UNFILTERED_STEERING),
            ),
            tyres = mapTyres(buffer, base),
            fuel = FuelData(
                currentLiters = buffer.getDouble(base + OFF_FUEL),
                capacityLiters = buffer.getDouble(base + OFF_FUEL_CAPACITY),
            ),
            timing = TimingData(
                currentLapTimeMs = 0L,  // Telemetry セグメントには含まれない。Scoring セグメントから取得
                lastLapTimeMs = 0L,
                bestLapTimeMs = 0L,
                sector1Ms = 0L,
                sector2Ms = 0L,
                currentLap = buffer.getInt(base + OFF_LAP_NUMBER),
            ),
            vehicle = VehicleData(
                localVelocityX = buffer.getDouble(base + OFF_LOCAL_VEL_X),
                localVelocityY = buffer.getDouble(base + OFF_LOCAL_VEL_Y),
                localVelocityZ = buffer.getDouble(base + OFF_LOCAL_VEL_Z),
                positionX = buffer.getDouble(base + OFF_POS_X),
                positionY = buffer.getDouble(base + OFF_POS_Y),
                positionZ = buffer.getDouble(base + OFF_POS_Z),
            ),
        )
    }

    private fun mapTyres(buffer: ByteBuffer, base: Int): TyreData {
        val wheels = WheelIndex.entries.associateWith { wheel ->
            val offset = base + OFF_WHEELS + (wheel.ordinal * WHEEL_STRIDE)
            // mTemperature[3] の中央値（インデックス1）を代表値として使用
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
