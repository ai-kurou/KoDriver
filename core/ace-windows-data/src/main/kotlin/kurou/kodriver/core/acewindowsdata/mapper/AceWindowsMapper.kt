package kurou.kodriver.core.acewindowsdata.mapper

import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsNearbyVehicleData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.WheelIndex
import java.nio.ByteBuffer
import kotlin.math.sqrt

/**
 * Assetto Corsa EVO の Graphics 共有メモリ (`Local\acevo_pmf_graphics`, `SPageFileGraphicEvo`) の
 * ByteBuffer を AceWindowsFuelData / AceWindowsFlagData / AceWindowsStatusData /
 * AceWindowsVehicleApproachData に変換する。
 *
 * `SPageFileGraphicEvo` は `char[33]` / `bool` を含むため固定オフセットが文書化されておらず
 * (docs/ace-windows-telemetry.md 参照)、_pack_=4 のアライメント規則 (4バイト境界に整列、
 * 8バイト型も pack=4 によって4バイト境界にキャップされる) に従って宣言順にオフセットを算出した。
 *
 * `flag` までの宣言順とオフセット:
 *   [+0]  packetId (int32)
 *   [+4]  status (int32)
 *   [+8]  focused_car_id_a/b, player_car_id_a/b (uint64 x4, 各8バイト)
 *   [+40] rpm (uint16)
 *   [+42] is_rpm_limiter_on 〜 is_max_charge_kj_per_lap_reached (bool x15)
 *   [+58] display_speed_kmh/mph/ms (int16 x3, +1バイトパディング後)
 *   [+64] pitspeeding_delta (float)
 *   [+68] gear_int (int16)
 *   [+72] rpm_percent 〜 car_ffb_multiplier (float x8, +2バイトパディング後)
 *   [+104] water_temperature_percent, water_pressure_bar, fuel_pressure_bar (float x3)
 *   [+116] water_temperature_c, air_temperature_c (int8 x2)
 *   [+120] oil_temperature_c 〜 turbo_boost_perc (float x9, +2バイトパディング後)
 *   [+156] steer_degrees (int32)
 *   [+160] current_km (float)
 *   [+164] total_km, total_driving_time_s (uint32 x2)
 *   [+172] time_of_day_hours/minutes/seconds, delta_time_ms, current_lap_time_ms,
 *          predicted_lap_time_ms (int32 x6)
 *   [+196] fuel_liter_current_quantity (float)
 *   [+200] fuel_liter_current_quantity_percent (float) ← Fuel 取得対象
 *   [+204] fuel_liter_per_km, km_per_fuel_liter (float x2)
 *   [+212] current_torque (float)
 *   [+216] current_bhp (int32)
 *   [+220] tyre_lf/rf/lr/rr (SMEvoTyreState x4, 各256バイト = 1024バイト) ← TyreCarcassTemperature 取得対象
 *     SMEvoTyreState 内の tyre_temperature_c (カーカス平均温度, ℃) は各要素内 [+12] オフセット
 *     (slip: float[+0], lock: bool[+4]+3パディング, tyre_pressure: float[+8], tyre_temperature_c: float[+12])。
 *     ホイール順は tyre_lf[+220]/tyre_rf[+476]/tyre_lr[+732]/tyre_rr[+988] で
 *     WheelIndex.FRONT_LEFT/FRONT_RIGHT/REAR_LEFT/REAR_RIGHT の ordinal 順と一致する。
 *   [+1244] npos, kers_charge_perc, kers_current_perc, control_lock_time (float x4)
 *   [+1260] car_damage (SMEvoDamageState, 128バイト)
 *   [+1388] car_location (int32)
 *   [+1392] pit_info (SMEvoPitInfo, 64バイト)
 *   [+1456] fuel_liter_used 〜 gear_rpm_window (float x8)
 *   [+1488] instrumentation, instrumentation_min_limit, instrumentation_max_limit,
 *           electronics, electronics_min_limit, electronics_max_limit,
 *           electronics_is_modifiable (各128バイト x7 = 896バイト)
 *   [+2384] total_lap_count (int32)
 *   [+2388] current_pos (uint32)
 *   [+2392] total_drivers (uint32)
 *   [+2396] last_laptime_ms (int32)
 *   [+2400] best_laptime_ms (int32)
 *   [+2404] flag (int32, ACEVO_FLAG_TYPE、自車提示) ← Flag 取得対象
 *
 * [+4] status (int32, ACEVO_STATUS) ← Status 取得対象
 * [+1388] car_location (int32, ACEVO_CAR_LOCATION) ← CarLocation 取得対象
 * [+24/+32] player_car_id_a/b (uint64) ← VehicleApproach で自車特定に使用
 *
 * `flag` 以降、`car_coordinates` までの宣言順とオフセット（**注意: 実機未検証**。
 * `docs/ace-windows-telemetry.md` 自身が「オフセットが必要な場合は ctypes / C++ の
 * offsetof で実行時に算出すること」と明記しているとおり、このセクションは公式ヘッダの
 * 実測値ではなく、ドキュメントの宣言順一覧から _pack_=4 のアライメント規則を適用して
 * 手計算したもの。1バイトでもズレると誤った車両を参照する可能性があるため、実機での
 * 検証が取れるまでは参考値として扱うこと）:
 *   [+2408] global_flag (int32)
 *   [+2412] max_gears (uint32), engine_type (int32)
 *   [+2420] has_kers, is_last_lap (bool x2)
 *   [+2422] performance_mode_name (char[33])
 *   [+2456] diff_coast_raw_value, diff_power_raw_value (float x2, +1バイトパディング後)
 *   [+2464] race_cut_gained_time_ms, distance_to_deadline (int32 x2)
 *   [+2472] race_cut_current_delta (float)
 *   [+2476] session_state (SMEvoSessionState, 256バイト)
 *   [+2732] timing_state (SMEvoTimingState, 256バイト)
 *   [+2988] player_ping 〜 player_fps_avg (int32 x8)
 *   [+3020] driver_name, driver_surname, car_model (char[33] x3)
 *   [+3119] is_in_pit_box, is_in_pit_lane, is_valid_lap (bool x3)
 *   [+3124] car_coordinates (float[60][3], 720バイト) ← VehicleApproach 取得対象、+2バイトパディング後
 *   [+3844] gap_ahead, gap_behind (float x2)
 *   [+3852] active_cars (uint8) ← VehicleApproach 取得対象
 *   [+3940] car_ids (uint64[60][2], 960バイト) ← VehicleApproach 取得対象、+3バイトパディング後
 */
internal object AceWindowsMapper {
    private const val OFF_STATUS = 4
    private const val OFF_PLAYER_CAR_ID_A = 24
    private const val OFF_PLAYER_CAR_ID_B = 32
    private const val OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT = 200
    private const val OFF_TYRE_LF = 220
    private const val TYRE_STATE_STRIDE = 256
    private const val OFF_TYRE_TEMPERATURE_C = 12
    private const val OFF_CAR_LOCATION = 1388
    private const val OFF_FLAG = 2404
    private const val OFF_CAR_COORDINATES = 3124
    private const val CAR_COORDINATES_STRIDE = 12
    private const val OFF_ACTIVE_CARS = 3852
    private const val OFF_CAR_IDS = 3940
    private const val CAR_ID_STRIDE = 16
    private const val MAX_CARS = 60
    private const val PERCENT_MULTIPLIER = 100

    // fuel_liter_current_quantity_percent は実際には 0.0〜1.0 の割合(fraction)で格納されている
    // (フィールド名に反する)。AceWindowsFuelData.remainingPercent の仕様(0.0〜100.0)に合わせて
    // ここで100倍する。
    fun mapFuel(buffer: ByteBuffer): AceWindowsFuelData =
        AceWindowsFuelData(
            remainingPercent =
                FuelPercent(buffer.getFloat(OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT).toDouble() * PERCENT_MULTIPLIER),
        )

    fun mapTyreCarcassTemperature(buffer: ByteBuffer): AceWindowsTyreCarcassTemperatureData =
        AceWindowsTyreCarcassTemperatureData(
            wheels =
                WheelIndex.entries.associateWith { wheel ->
                    val tyreStateBase = OFF_TYRE_LF + wheel.ordinal * TYRE_STATE_STRIDE
                    CelsiusReading(buffer.getFloat(tyreStateBase + OFF_TYRE_TEMPERATURE_C))
                },
        )

    fun mapFlag(buffer: ByteBuffer): AceWindowsFlagData =
        AceWindowsFlagData(
            flag = AceWindowsFlagType.fromRaw(buffer.getInt(OFF_FLAG)),
        )

    fun mapStatus(buffer: ByteBuffer): AceWindowsStatusData =
        AceWindowsStatusData(
            status = AceWindowsStatusType.fromRaw(buffer.getInt(OFF_STATUS)),
            carLocation = AceWindowsCarLocation.fromRaw(buffer.getInt(OFF_CAR_LOCATION)),
        )

    // car_coordinates には自車の座標も含まれるため、car_ids を player_car_id_a/b と
    // 突き合わせて自車のインデックスを特定し、他の各アクティブ車両とのワールド座標上の
    // 直線距離を求める。ACE の Graphics 共有メモリには自車の向き（yaw）に相当する
    // フィールドが無いため、LMU のような左右の並走判定はできない。
    fun mapVehicleApproach(buffer: ByteBuffer): AceWindowsVehicleApproachData {
        val activeCars = (buffer.get(OFF_ACTIVE_CARS).toInt() and BYTE_MASK).coerceAtMost(MAX_CARS)
        val playerCarIdA = buffer.getLong(OFF_PLAYER_CAR_ID_A)
        val playerCarIdB = buffer.getLong(OFF_PLAYER_CAR_ID_B)
        val ownIndex =
            (0 until activeCars).firstOrNull { i ->
                buffer.getLong(OFF_CAR_IDS + i * CAR_ID_STRIDE) == playerCarIdA &&
                    buffer.getLong(OFF_CAR_IDS + i * CAR_ID_STRIDE + Long.SIZE_BYTES) == playerCarIdB
            } ?: return AceWindowsVehicleApproachData(nearbyVehicles = emptyList())

        val (ownX, ownY, ownZ) = carCoordinates(buffer, ownIndex)
        val nearbyVehicles =
            (0 until activeCars)
                .filter { it != ownIndex }
                .map { i ->
                    val (x, y, z) = carCoordinates(buffer, i)
                    val dx = (x - ownX).toDouble()
                    val dy = (y - ownY).toDouble()
                    val dz = (z - ownZ).toDouble()
                    AceWindowsNearbyVehicleData(distanceMeters = sqrt(dx * dx + dy * dy + dz * dz))
                }
        return AceWindowsVehicleApproachData(nearbyVehicles = nearbyVehicles)
    }

    private fun carCoordinates(
        buffer: ByteBuffer,
        index: Int,
    ): Triple<Float, Float, Float> {
        val base = OFF_CAR_COORDINATES + index * CAR_COORDINATES_STRIDE
        return Triple(
            buffer.getFloat(base),
            buffer.getFloat(base + Float.SIZE_BYTES),
            buffer.getFloat(base + Float.SIZE_BYTES * 2),
        )
    }

    private const val BYTE_MASK = 0xFF
}
