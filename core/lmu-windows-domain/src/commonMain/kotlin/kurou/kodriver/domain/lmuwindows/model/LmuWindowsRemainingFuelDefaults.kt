package kurou.kodriver.domain.lmuwindows.model

const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT = 30

/** 燃料残量警告の閾値（残量 %）として設定できる範囲。 */
const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN = 10
const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX = 90

const val LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT = "燃料は残り{percent}パーセント"
const val LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER = "{percent}"

/** LMU の手動読み上げで使う燃料残量の既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT = "燃料は残り{percent}パーセント"

/** LMU の手動読み上げで使う燃料の残り周回数の既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT = "燃料は残り約{laps}周"
const val LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT = "燃料残り1周未満"
