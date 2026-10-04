package kurou.kodriver.domain.model

val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT = Celsius(95)

/** LMU のタイヤ過熱警告の高温閾値スライダーの下限（摂氏）。 */
val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN = Celsius(90)

/** LMU のタイヤ過熱警告の高温閾値スライダーの上限（摂氏）。 */
val LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX = Celsius(110)

/** タイヤ過熱警告の既定文言。DataStore の初期値として使用する。 */
const val LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT = "タイヤ過熱警告"

/** タイヤ低温警告の既定文言。DataStore の初期値として使用する。 */
const val LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT = "タイヤ低温警告"
