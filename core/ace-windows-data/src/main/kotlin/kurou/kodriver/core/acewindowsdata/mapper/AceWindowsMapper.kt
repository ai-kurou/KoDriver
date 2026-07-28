package kurou.kodriver.core.acewindowsdata.mapper

import kurou.kodriver.domain.model.AceWindowsFuelData
import java.nio.ByteBuffer

/**
 * Assetto Corsa EVO の Graphics 共有メモリ (`Local\acevo_pmf_graphics`, `SPageFileGraphicEvo`) の
 * ByteBuffer を AceWindowsFuelData に変換する。
 *
 * `SPageFileGraphicEvo` は `char[33]` / `bool` を含むため固定オフセットが文書化されておらず
 * (docs/ace-windows-telemetry.md 参照)、_pack_=4 のアライメント規則 (4バイト境界に整列、
 * 8バイト型も pack=4 によって4バイト境界にキャップされる) に従って宣言順にオフセットを算出した。
 *
 * `fuel_liter_current_quantity_percent` までの宣言順とオフセット:
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
 *   [+200] fuel_liter_current_quantity_percent (float) ← 取得対象
 */
internal object AceWindowsMapper {
    private const val OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT = 200

    fun map(buffer: ByteBuffer): AceWindowsFuelData =
        AceWindowsFuelData(
            remainingPercent = buffer.getFloat(OFF_FUEL_LITER_CURRENT_QUANTITY_PERCENT).toDouble(),
        )
}
