package kurou.kodriver.domain.model

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値（摂氏）のデフォルト値。 */
val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT = Celsius(90)

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN = Celsius(90)

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
val ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX = Celsius(110)

/** ACE (Assetto Corsa EVO) のタイヤ温度警告文言の摂氏温度プレースホルダー。 */
const val ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER = "{celsius}"

/** ACE (Assetto Corsa EVO) のタイヤ過熱警告の読み上げ文言のデフォルト値。 */
const val ACE_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT = "タイヤ過熱 {celsius}度"
