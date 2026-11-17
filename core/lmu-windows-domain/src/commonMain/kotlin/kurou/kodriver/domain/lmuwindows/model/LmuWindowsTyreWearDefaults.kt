package kurou.kodriver.domain.lmuwindows.model

/**
 * タイヤ摩耗警告のデフォルト閾値（タイヤ残存率 %。この値以下になると警告する）。
 *
 * DataStore のデフォルト値（LmuWindowsTyreWearPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
public const val LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT: Int = 30

/** タイヤ摩耗警告の閾値（%）の設定可能な下限。 */
public const val LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_MIN: Int = 10

/** タイヤ摩耗警告の閾値（%）の設定可能な上限。 */
public const val LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_MAX: Int = 90

/** タイヤ残存率警告の既定文言。DataStore の初期値として使用する。 */
public const val LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT: String = "タイヤは残り{percent}%以下"

/** タイヤ残存率警告の閾値（%）を埋め込むプレースホルダー。 */
public const val LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER: String = "{percent}"

/** 手動読み上げの現在のタイヤ残存率の既定文言。 */
internal const val LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT = "タイヤは残り{percent}%"
