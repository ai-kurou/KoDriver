package kurou.kodriver.domain.gt7ps5.model

public const val GT7_PS5_REMAINING_FUEL_LAPS_DEFAULT: Int = 3
public const val GT7_PS5_REMAINING_FUEL_LAPS_MIN: Int = 1
public const val GT7_PS5_REMAINING_FUEL_LAPS_MAX: Int = 5

public const val GT7_PS5_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT: String = "燃料は残り約{laps}周"
public const val GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT: String = "燃料は残り1周未満"
public const val GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER: String = "{laps}"

/** 手動読み上げの燃料残り周回数の既定文言。 */
internal const val GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_LAPS_TEXT_DEFAULT = "燃料は残り約{laps}周"
