package kurou.kodriver.domain.model

/** ACE 車両接近アナウンスの、前後方向の接近判定しきい値のデフォルト値。単位は meters。 */
const val ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT = 5.0

/** ACE 車両接近アナウンスの、左右方向の接近判定しきい値のデフォルト値。単位は meters。 */
const val ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT = 5.0

/** ACE 車両接近アナウンスの、接近開始時読み上げの有効/無効デフォルト値。 */
const val ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT = true

/** ACE 車両接近アナウンスの、接近開始時読み上げの読み上げ文言デフォルト値。 */
val ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_TYPE_DEFAULT =
    VehicleApproachStartReadoutType.CAR_LEFT_RIGHT
