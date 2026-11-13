package kurou.kodriver.domain.lmuwindows.model

/** タイヤ残存率がこの値（%）未満なら、デバッグ状態画面で「温まっている」（WARM）とみなす。 */
const val LMU_WINDOWS_TYRE_HEALTHY_REMAINING_PERCENT = 75.0

/** ブレーキ温度がこの値（℃）未満なら、デバッグ状態画面で「冷えている」（OK）とみなす。 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS = 400.0

/** ブレーキ温度がこの値（℃）以上なら、デバッグ状態画面で「高温」（HOT）とみなす。 */
const val LMU_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS = 1000.0
