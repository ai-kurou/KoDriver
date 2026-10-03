package kurou.kodriver.domain.model

/**
 * バーチャルエナジー残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（LmuWindowsRemainingVirtualEnergyPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT = 30

/** バーチャルエナジー残量警告の既定文言。DataStore の初期値として使用する。 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT = "バーチャルエナジー残量{percent}%以下"

/** バーチャルエナジー残量警告の閾値（%）を埋め込むプレースホルダー。 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER = "{percent}"
