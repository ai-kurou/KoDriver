package kurou.kodriver.domain.lmuwindows.model

/**
 * ブレーキ過熱警告の閾値スライダーの可動範囲（摂氏）。全車両クラス共通。
 * デフォルト値自体はクラスごとに異なるため [lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault] を参照。
 */
public const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN: Int = 600
public const val LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX: Int = 1000

/** ブレーキ過熱警告の既定文言。 */
public const val LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT: String = "ブレーキ温度は{celsius}℃以上"

/** 実測温度ではなく、設定した閾値（℃）を埋め込むプレースホルダー。 */
public const val LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER: String = "{celsius}"

/** 手動読み上げの現在のブレーキ温度の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_BRAKE_TEMPERATURE_TEXT_DEFAULT = "ブレーキ温度は{celsius}℃"
