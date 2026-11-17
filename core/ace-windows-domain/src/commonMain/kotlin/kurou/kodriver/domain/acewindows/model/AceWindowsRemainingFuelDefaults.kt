package kurou.kodriver.domain.acewindows.model

/**
 * 燃料残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値（AceWindowsRemainingFuelPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
public const val ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT: Int = 30

/** ACE の燃料残量警告の閾値（%）の設定可能な下限。 */
public const val ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN: Int = 5

/** ACE の燃料残量警告の閾値（%）の設定可能な上限。 */
public const val ACE_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX: Int = 90

/** ACE の燃料残量警告のデフォルト読み上げ文言。 */
public const val ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT: String = "燃料は残り{percent}%"

/** ACE の燃料残量警告文言で残量パーセントを表すプレースホルダー。 */
public const val ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER: String = "{percent}"

/** ACE の手動読み上げで使う燃料残量の既定文言。 */
internal const val ACE_WINDOWS_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT = "燃料は残り{percent}%"
