package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.Celsius

/** ACE (Assetto Corsa EVO) のブレーキ過熱警告の高温閾値（摂氏）のデフォルト値。 */
public val ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT: Celsius = Celsius(800)

/** ACE (Assetto Corsa EVO) のブレーキ過熱警告の高温閾値スライダーの下限（摂氏）。 */
public val ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN: Celsius = Celsius(600)

/** ACE (Assetto Corsa EVO) のブレーキ過熱警告の高温閾値スライダーの上限（摂氏）。 */
public val ACE_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX: Celsius = Celsius(1000)

/** ACE (Assetto Corsa EVO) のブレーキ温度警告文言の摂氏温度プレースホルダー。 */
public const val ACE_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER: String = "{celsius}"

/** ACE (Assetto Corsa EVO) のブレーキ過熱警告の読み上げ文言のデフォルト値。 */
public const val ACE_WINDOWS_BRAKE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT: String = "ブレーキ温度は{celsius}℃以上"
