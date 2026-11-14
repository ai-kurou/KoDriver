package kurou.kodriver.domain.lmuwindows.model

/** 前方の秒数が入るプレースホルダー。単位なしの秒数（例「1.5」）に置き換わる。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER = "{ahead}"

/** 後方の秒数が入るプレースホルダー。単位なしの秒数（例「2.3」）に置き換わる。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER = "{behind}"

/** 前方の節（「前方{ahead}秒」）。先頭のときは節全体が [LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT] に置き換わる。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_CLAUSE = "前方${LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_PLACEHOLDER}秒"

/** 後方の節（「後方{behind}秒」）。最後尾のときは節全体が [LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT] に置き換わる。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_CLAUSE = "後方${LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_PLACEHOLDER}秒"

/** 編集画面で挿入する前方・後方の節の組。既定文言もこの組を使う。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_PLACEHOLDERS =
    "$LMU_WINDOWS_CLASS_CAR_GAP_AHEAD_CLAUSE、$LMU_WINDOWS_CLASS_CAR_GAP_BEHIND_CLAUSE"

/** LMU の手動読み上げで使うクラス内前後車とのギャップの既定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TEXT_DEFAULT = LMU_WINDOWS_CLASS_CAR_GAP_PLACEHOLDERS

/** クラス内先頭（前方車なし）のときの前方の節全体の固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_TOP_TEXT_DEFAULT = "クラス内先頭です"

/** クラス内最後尾（後方車なし）のときの後方の節全体の固定文言。 */
const val LMU_WINDOWS_MANUAL_READOUT_CLASS_CAR_GAP_LAST_TEXT_DEFAULT = "クラス内最後尾です"

/** 節を含まない自由文言で、車なし時にプレースホルダー単体へ入れる文言。 */
const val LMU_WINDOWS_CLASS_CAR_GAP_NONE_VALUE_TEXT = "なし"
