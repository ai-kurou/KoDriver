package kurou.kodriver.domain.acewindows.model

/**
 * 残り燃料警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（AceWindowsRemainingFuelPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT = 30

/** ACE の燃料残量警告のデフォルト読み上げ文言。 */
const val ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT = "燃料は残り{percent}パーセント"

/** ACE の燃料残量警告文言で残量パーセントを表すプレースホルダー。 */
const val ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER = "{percent}"

/** ACE の手動読み上げで使う燃料残量の既定文言。 */
const val ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT = "燃料は残り{percent}パーセント"
