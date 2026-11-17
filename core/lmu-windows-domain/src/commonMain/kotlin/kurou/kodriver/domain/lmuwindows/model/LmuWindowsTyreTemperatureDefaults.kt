package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.Celsius

public val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT: Celsius = Celsius(95)

/** LMU のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
public val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN: Celsius = Celsius(90)

/** LMU のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
public val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX: Celsius = Celsius(110)

/** 警告判定時の全輪の最高カーカス温度（整数の摂氏）に置換するプレースホルダー。 */
public const val LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER: String = "{celsius}"

/** 温度プレースホルダーを含むタイヤ過熱警告の既定文言。DataStore の初期値とリセット値に使用する。 */
public const val LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT: String =
    "タイヤ過熱${LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER}℃"

/** 温度プレースホルダーを含むタイヤ低温警告の既定文言。DataStore の初期値とリセット値に使用する。 */
public const val LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT: String =
    "タイヤ低温${LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER}℃"

/** 手動読み上げの現在のタイヤ温度の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT = "タイヤ温度は{celsius}℃"
