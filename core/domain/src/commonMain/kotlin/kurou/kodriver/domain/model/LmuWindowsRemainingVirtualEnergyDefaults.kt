package kurou.kodriver.domain.model

/**
 * バーチャルエナジー残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（LmuWindowsRemainingVirtualEnergyPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_DEFAULT_THRESHOLD_PERCENTAGE = 50
