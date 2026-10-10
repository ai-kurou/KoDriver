package kurou.kodriver.domain.model

/**
 * ブレーキ残量警告の閾値スライダーの可動範囲（%）。全車両クラス共通。
 * デフォルト値自体はクラスごとに持てるため [lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault] を参照。
 */
const val LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN = 5
const val LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX = 50

/** ブレーキ残量警告の既定文言。 */
const val LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT = "ブレーキ残量{percent}%以下"

/** 実測の残量ではなく、設定した閾値（%）を埋め込むプレースホルダー。 */
const val LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER = "{percent}"
