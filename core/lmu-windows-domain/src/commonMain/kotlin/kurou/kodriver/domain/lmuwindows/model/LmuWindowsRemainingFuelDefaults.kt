package kurou.kodriver.domain.lmuwindows.model

public const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT: Int = 30

/** 燃料残量警告の閾値（残量 %）として設定できる範囲。 */
public const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN: Int = 10
public const val LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX: Int = 90

public const val LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT: String = "燃料は残り{percent}%"
public const val LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER: String = "{percent}"

/** LMU の手動読み上げで使う燃料残量の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT = "燃料は残り{percent}%"

/** LMU の手動読み上げで使う燃料残り周回数の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_TEXT_DEFAULT = "燃料は残り約{laps}周"
internal const val LMU_WINDOWS_MANUAL_READOUT_FUEL_LAPS_EMPTY_TEXT_DEFAULT = "燃料は残り1周未満"
