package kurou.kodriver.domain.model

/**
 * 残り燃料警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（AceWindowsRemainingFuelPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val ACE_WINDOWS_REMAINING_FUEL_DEFAULT_THRESHOLD_PERCENTAGE = 30
