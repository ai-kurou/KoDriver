package kurou.kodriver.domain.lmuwindows.model

public const val LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT: Double = 5.0
public const val LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT: Double = 5.0
public const val LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_MINIMUM_METERS_DEFAULT: Double = 1.0
public const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT: Int = 7
public const val LMU_WINDOWS_VEHICLE_APPROACH_SKIP_FIRST_LAP_DEFAULT: Boolean = true

/** 車両接近の前後方向閾値（メートル）の設定可能な下限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_MIN: Double = 0.1

/** 車両接近の前後方向閾値（メートル）の設定可能な上限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_MAX: Double = 10.0

/** 車両接近の左右方向閾値（メートル）の設定可能な下限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_MIN: Double = 2.0

/** 車両接近の左右方向閾値（メートル）の設定可能な上限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_MAX: Double = 8.0

/** 車両接近の継続時間（秒）の設定可能な下限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_MIN: Int = 4

/** 車両接近の継続時間（秒）の設定可能な上限。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_MAX: Int = 10

/** LMU の左車両接近開始時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT: String = "カーレフト"

/** LMU の右車両接近開始時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT: String = "カーライト"

/** LMU の左車両接近継続時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT: String = "キープライト"

/** LMU の右車両接近継続時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT: String = "キープレフト"
