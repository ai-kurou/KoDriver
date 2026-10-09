package kurou.kodriver.domain.model

/** GT7 のタイヤ過熱警告の高温閾値（摂氏）のデフォルト値。 */
val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT = Celsius(100)

/** GT7 のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN = Celsius(90)

/** GT7 のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
val GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX = Celsius(110)

/** タイヤ温度警告文言の摂氏温度プレースホルダー。 */
const val GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER = "{celsius}"

/** GT7 のタイヤ過熱警告の読み上げ文言のデフォルト値。 */
const val GT7_PS5_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT = "タイヤ過熱 {celsius}度"
