package kurou.kodriver.domain.model

/**
 * ブレーキ過熱警告のデフォルト閾値（Celsius）と、ユーザーが設定可能な範囲。
 *
 * DataStore のデフォルト値（LmuWindowsBrakeTemperaturePreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT = 700
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN = 500
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX = 900
