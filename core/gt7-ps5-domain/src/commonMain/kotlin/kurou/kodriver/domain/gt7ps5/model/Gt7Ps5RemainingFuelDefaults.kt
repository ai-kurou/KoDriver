package kurou.kodriver.domain.gt7ps5.model

/**
 * 燃料残量警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値・詳細設定画面のリセット値・Narrator の購読初期値が同じ値を参照できるよう、
 * この一箇所にのみ定義する。
 */
public const val GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT: Int = 30

/** 燃料残量警告閾値の設定可能な下限（残量 %）。 */
public const val GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN: Int = 5

/** 燃料残量警告閾値の設定可能な上限（残量 %）。 */
public const val GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX: Int = 90

public const val GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT: String = "燃料は残り{percent}%"
public const val GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER: String = "{percent}"

/** 手動読み上げの燃料残量の既定文言。 */
internal const val GT7_PS5_MANUAL_READOUT_REMAINING_FUEL_TEXT_DEFAULT = "燃料は残り{percent}%"
