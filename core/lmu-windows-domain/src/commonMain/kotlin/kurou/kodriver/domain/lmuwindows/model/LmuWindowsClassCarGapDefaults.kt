package kurou.kodriver.domain.lmuwindows.model

/** クラス内前方車とのギャップ部分に置き換わるプレースホルダー。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER = "{ahead}"

/** クラス内後方車とのギャップ部分に置き換わるプレースホルダー。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER = "{behind}"

/** LMU の手動読み上げで使うクラス内前後車とのギャップの既定文言。前方の後に後方を読み上げる。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT = "{ahead}、{behind}"

/** 前方車とのギャップ部分の文。`{seconds}` は小数1桁の秒に置き換わる。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_PHRASE = "クラス内前方{seconds}秒"

/** 後方車とのギャップ部分の文。`{seconds}` は小数1桁の秒に置き換わる。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_PHRASE = "後方{seconds}秒"

/** クラス内先頭（前方車なし）のときの前方部分の固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT = "クラス内先頭です"

/** クラス内最後尾（後方車なし）のときの後方部分の固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT = "クラス内最後尾です"
