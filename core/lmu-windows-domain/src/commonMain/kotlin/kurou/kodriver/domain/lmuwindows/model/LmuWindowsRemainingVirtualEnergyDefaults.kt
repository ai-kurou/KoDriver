package kurou.kodriver.domain.lmuwindows.model

/**
 * バーチャルエネルギー残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（LmuWindowsRemainingVirtualEnergyPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
public const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT: Int = 30

/** バーチャルエネルギー残量警告の閾値（%）の設定可能な下限。 */
public const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_MIN: Int = 10

/** バーチャルエネルギー残量警告の閾値（%）の設定可能な上限。 */
public const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_MAX: Int = 90

/** バーチャルエネルギー残量警告の既定文言。DataStore の初期値として使用する。 */
public const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT: String = "バーチャルエネルギーは残り{percent}%以下"

/** バーチャルエネルギー残量警告の閾値（%）を埋め込むプレースホルダー。 */
public const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER: String = "{percent}"

/** 手動読み上げの現在のバーチャルエネルギー残量の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT =
    "バーチャルエネルギーは残り{percent}%"
