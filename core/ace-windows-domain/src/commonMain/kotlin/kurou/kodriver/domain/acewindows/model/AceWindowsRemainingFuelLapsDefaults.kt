package kurou.kodriver.domain.acewindows.model

public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT: Int = 3
public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_MIN: Int = 1
public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_MAX: Int = 5
internal const val ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_MAX = 1_000

public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT: String = "燃料は残り約{laps}周"
public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT: String = "燃料は残り1周未満"
public const val ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER: String = "{laps}"

/** ACE の手動読み上げで使う燃料残り周回数の既定文言。 */
internal const val ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT = "燃料は残り約{laps}周"
