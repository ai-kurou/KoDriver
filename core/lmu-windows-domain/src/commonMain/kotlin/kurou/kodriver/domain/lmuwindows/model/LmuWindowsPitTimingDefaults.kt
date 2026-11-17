package kurou.kodriver.domain.lmuwindows.model

public const val LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT: Int = 3
public const val LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT: Int = 3
public const val LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT: Int = 3
public const val LMU_WINDOWS_PIT_TIMING_LAPS_MIN: Int = 1
public const val LMU_WINDOWS_PIT_TIMING_LAPS_MAX: Int = 5

/** 手動読み上げの残り周回数をデータとして扱う上限。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX = 1_000

/** 手動読み上げの残り周回数上限との比較で許容する浮動小数点誤差（周）。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_REMAINING_LAPS_MAX_ROUNDING_TOLERANCE = 1e-9

public const val LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT: String = "バーチャルエネルギー切れのため{laps}周以内にピットイン"
public const val LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT: String = "バーチャルエネルギー切れのため必ずピットイン"
public const val LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER: String = "{laps}"

public const val LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT: String = "タイヤ摩耗のため{laps}周以内にピットイン"
public const val LMU_WINDOWS_PIT_TIMING_FUEL_READOUT_TEXT_DEFAULT: String = "燃料は残り約{laps}周"
public const val LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT: String = "タイヤ摩耗のため必ずピットイン"
public const val LMU_WINDOWS_PIT_TIMING_FUEL_IMMINENT_READOUT_TEXT_DEFAULT: String = "燃料は残り1周未満"

/** 手動読み上げのバーチャルエネルギー残り周回数の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_TEXT_DEFAULT = "バーチャルエネルギーは残り約{laps}周"
internal const val LMU_WINDOWS_MANUAL_READOUT_VIRTUAL_ENERGY_LAPS_EMPTY_TEXT_DEFAULT = "バーチャルエネルギーは残り1周未満"

/** 手動読み上げのタイヤ摩耗残り周回数の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_TEXT_DEFAULT = "タイヤは残り約{laps}周"
internal const val LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_LAPS_EMPTY_TEXT_DEFAULT = "タイヤは残り1周未満"
