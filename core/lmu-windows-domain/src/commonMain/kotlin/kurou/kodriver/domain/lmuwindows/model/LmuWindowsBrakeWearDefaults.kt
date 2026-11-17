package kurou.kodriver.domain.lmuwindows.model

/**
 * ブレーキ残量警告の閾値スライダーの可動範囲（%）。全車両クラス共通。
 * デフォルト値自体はクラスごとに持てるため [lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault] を参照。
 */
public const val LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN: Int = 5
public const val LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX: Int = 50

/** ブレーキ残量警告の既定文言。 */
public const val LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT: String = "ブレーキは残り{percent}%以下"

/** 実測の残量ではなく、設定した閾値（%）を埋め込むプレースホルダー。 */
public const val LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER: String = "{percent}"

/** 手動読み上げの現在のブレーキ残量の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_BRAKE_WEAR_TEXT_DEFAULT = "ブレーキは残り{percent}%"
