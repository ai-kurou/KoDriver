package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.Celsius

/** GT7 のタイヤ過熱警告の高温閾値（摂氏）のデフォルト値。 */
public val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT: Celsius = Celsius(105)

/** GT7 のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
public val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN: Celsius = Celsius(90)

/** GT7 のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
public val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX: Celsius = Celsius(120)

/** タイヤ温度警告文言の摂氏温度プレースホルダー。 */
public const val GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER: String = "{celsius}"

/** GT7 のタイヤ過熱警告の読み上げ文言のデフォルト値。 */
public const val GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT: String = "タイヤ過熱{celsius}℃"

/** 手動読み上げのタイヤ温度の既定文言。 */
internal const val GT7_PS5_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT = "タイヤ温度は{celsius}℃"
