package kurou.kodriver.domain.lmuwindows.model

/**
 * タイヤ摩耗警告のデフォルト閾値（タイヤ残存率 %。この値以下になると警告する）。
 *
 * DataStore のデフォルト値（LmuWindowsTyreWearPreferences）・詳細設定画面のリセット値・
 * Narrator の購読初期値が同じ値を参照できるよう、この一箇所にのみ定義する。
 */
const val LMU_WINDOWS_TYRE_WEAR_THRESHOLD_PERCENTAGE_DEFAULT = 30

/** タイヤ残存率警告の既定文言。DataStore の初期値として使用する。 */
const val LMU_WINDOWS_TYRE_WEAR_READOUT_TEXT_DEFAULT = "タイヤ残存率{percent}%以下"

/** タイヤ残存率警告の閾値（%）を埋め込むプレースホルダー。 */
const val LMU_WINDOWS_TYRE_WEAR_PERCENT_PLACEHOLDER = "{percent}"

/** 手動読み上げの現在のタイヤ残存率の既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT = "タイヤ摩耗残り{percent}%"
