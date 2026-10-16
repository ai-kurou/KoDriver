package kurou.kodriver.domain.model

/**
 * 残り燃料警告のデフォルト閾値（残量 %）。
 *
 * DataStore のデフォルト値・詳細設定画面のリセット値・Narrator の購読初期値が同じ値を参照できるよう、
 * この一箇所にのみ定義する。
 */
const val GT7_PS5_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT = 30

const val GT7_PS5_REMAINING_FUEL_READOUT_TEXT_DEFAULT = "燃料は残り{percent}パーセント"
const val GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER = "{percent}"

/** 手動読み上げの対応項目がない場合の固定文言。 */
const val GT7_PS5_MANUAL_READOUT_NO_ITEMS_TEXT = "読み上げる項目がありません"
