package kurou.kodriver.domain.model

/**
 * ブレーキ過熱警告の閾値スライダーの可動範囲（摂氏）。全車両クラス共通。
 * デフォルト値自体はクラスごとに異なるため [lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault] を参照。
 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN = 600
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX = 1000

/** ブレーキ過熱警告の既定文言。 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT = "ブレーキ温度{celsius}℃以上"

/** 実測温度ではなく、設定した閾値（℃）を埋め込むプレースホルダー。 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER = "{celsius}"
