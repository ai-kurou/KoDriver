package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.Celsius

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値（摂氏）のデフォルト値。 */
public val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT: Celsius = Celsius(90)

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
public val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN: Celsius = Celsius(90)

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
public val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX: Celsius = Celsius(110)

/** ACE (Assetto Corsa EVO) のタイヤ温度警告文言の摂氏温度プレースホルダー。 */
public const val ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER: String = "{celsius}"

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の読み上げ文言のデフォルト値。 */
public const val ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT: String = "タイヤ過熱{celsius}℃"

/** ACE の手動読み上げで使うタイヤ温度の既定文言。 */
internal const val ACE_WINDOWS_MANUAL_READOUT_TYRE_TEMPERATURE_TEXT_DEFAULT = "タイヤ温度は{celsius}℃"
