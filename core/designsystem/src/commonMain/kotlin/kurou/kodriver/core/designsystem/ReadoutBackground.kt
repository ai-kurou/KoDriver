package kurou.kodriver.core.designsystem

import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.best_lap_row_background
import kurou.kodriver.core.designsystem.generated.resources.brake_row_background
import kurou.kodriver.core.designsystem.generated.resources.flag_row_background
import kurou.kodriver.core.designsystem.generated.resources.fuel_row_background
import kurou.kodriver.core.designsystem.generated.resources.pit_timing_row_background
import kurou.kodriver.core.designsystem.generated.resources.tyre_row_background
import kurou.kodriver.core.designsystem.generated.resources.vehicle_approach_row_background
import kurou.kodriver.core.designsystem.generated.resources.virtual_energy_row_background
import org.jetbrains.compose.resources.DrawableResource

/**
 * 読み上げ項目の行カードの右側に背景として表示する画像の種類。
 * どの項目にどの画像を使うかは各 feature が決め、画像そのものと描画は designsystem で共有する。
 */
public enum class ReadoutBackground(
    public val drawable: DrawableResource,
) {
    /** タイヤ関連（温度・摩耗）。 */
    TYRE(Res.drawable.tyre_row_background),

    /** ブレーキ関連（温度・摩耗）。 */
    BRAKE(Res.drawable.brake_row_background),

    /** 燃料関連（残量・残り周回数）。 */
    FUEL(Res.drawable.fuel_row_background),

    /** 自己ベストラップ（ドライバー）。 */
    BEST_LAP(Res.drawable.best_lap_row_background),

    /** フラッグ。 */
    FLAG(Res.drawable.flag_row_background),

    /** バーチャルエネルギー残量。 */
    VIRTUAL_ENERGY(Res.drawable.virtual_energy_row_background),

    /** 車両接近。 */
    VEHICLE_APPROACH(Res.drawable.vehicle_approach_row_background),

    /** ピットタイミング。 */
    PIT_TIMING(Res.drawable.pit_timing_row_background),
}
