package kurou.kodriver.domain.model

/**
 * ACE 車両接近アナウンスの、接近判定しきい値のデフォルト値。単位は meters。
 *
 * ACE の共有メモリには自車の向きに相当するフィールドが存在せず、自車中心から相手車両中心までの
 * 合成距離のみが取得できるため、LMU のような前後・左右を区別した閾値は持たず単一の閾値のみを持つ。
 */
const val ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT = 5.0

/** ACE 車両接近アナウンスの、接近開始時読み上げの有効/無効デフォルト値。 */
const val ACE_WINDOWS_VEHICLE_APPROACH_START_READOUT_ENABLED_DEFAULT = true
