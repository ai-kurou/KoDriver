package kurou.kodriver.domain.lmuwindows.model

/** クラス内前後車とのギャップ文言の秒数プレースホルダー。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_SECONDS_PLACEHOLDER = "{seconds}"

/** LMU の手動読み上げで使うクラス内前方車とのギャップの既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_AHEAD_TEXT_DEFAULT = "クラス内前方{seconds}秒"

/** LMU の手動読み上げで使うクラス内後方車とのギャップの既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_BEHIND_TEXT_DEFAULT = "クラス内後方{seconds}秒"

/** クラス内トップ（前方車なし）のときの固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT = "クラス内トップです"

/** クラス内最後尾（後方車なし）のときの固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT = "クラス内最後尾です"
