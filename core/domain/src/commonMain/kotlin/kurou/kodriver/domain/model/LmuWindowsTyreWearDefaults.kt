package kurou.kodriver.domain.model

/**
 * タイヤ摩耗警告のデフォルト閾値（摩耗率 %）。
 *
 * DataStore のデフォルト値（LmuWindowsTyreWearPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_TYRE_WEAR_DEFAULT_THRESHOLD_PERCENTAGE = 50
