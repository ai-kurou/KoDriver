package kurou.kodriver.domain.lmuwindows.model

/**
 * バーチャルエネルギー残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（LmuWindowsRemainingVirtualEnergyPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_THRESHOLD_PERCENTAGE_DEFAULT = 30

/** バーチャルエネルギー残量警告の既定文言。DataStore の初期値として使用する。 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT = "バーチャルエネルギー残量{percent}%以下"

/** バーチャルエネルギー残量警告の閾値（%）を埋め込むプレースホルダー。 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER = "{percent}"

/** 手動読み上げの現在のバーチャルエネルギー残量の既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT =
    "バーチャルエネルギー残量は{percent}パーセント"

/** 手動読み上げで値がない場合の文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_NO_DATA_TEXT = "データがありません"
