package kurou.kodriver.core.acewindowsdata.mapper

import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import java.nio.ByteBuffer

/**
 * Assetto Corsa EVO の Graphics 共有メモリ (`Local\acevo_pmf_graphics`, `SPageFileGraphicEvo`) の
 * ByteBuffer を AceWindowsFuelData / AceWindowsFlagData / AceWindowsStatusData に変換する。
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
 *   [+220] tyre_lf/rf/lr/rr (SMEvoTyreState x4, 各256バイト = 1024バイト)
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
 */
internal object AceWindowsMapper {
    private const val OFF_STATUS = 4
    private const val OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT = 200
    private const val OFF_CAR_LOCATION = 1388
    private const val OFF_FLAG = 2404
    private const val PERCENT_MULTIPLIER = 100

    // fuel_liter_current_quantity_percent は実際には 0.0〜1.0 の割合(fraction)で格納されている
    // (フィールド名に反する)。AceWindowsFuelData.remainingPercent の仕様(0.0〜100.0)に合わせて
    // ここで100倍する。
    fun map(buffer: ByteBuffer): AceWindowsFuelData =
        AceWindowsFuelData(
            remainingPercent =
                buffer.getFloat(OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT).toDouble() * PERCENT_MULTIPLIER,
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
}
