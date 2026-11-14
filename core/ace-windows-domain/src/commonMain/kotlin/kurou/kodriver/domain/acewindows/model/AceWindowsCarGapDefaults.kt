package kurou.kodriver.domain.acewindows.model

/** 前後車ギャップ文言の前方プレースホルダー。単位なしの秒数（例「1.5」）に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER = "{ahead}"

/** 前後車ギャップ文言の後方プレースホルダー。単位なしの秒数（例「2.3」）に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER = "{behind}"

/** 前方の節（「前方{ahead}秒」）。車なし時は節全体が [ACE_WINDOWS_CAR_GAP_AHEAD_NONE_TEXT] に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_AHEAD_CLAUSE = "前方${ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER}秒"

/** 後方の節（「後方{behind}秒」）。車なし時は節全体が [ACE_WINDOWS_CAR_GAP_BEHIND_NONE_TEXT] に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_BEHIND_CLAUSE = "後方${ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER}秒"

/** 編集画面で挿入する前方・後方の節の組。既定文言もこの組を使う。 */
const val ACE_WINDOWS_CAR_GAP_PLACEHOLDERS =
    "$ACE_WINDOWS_CAR_GAP_AHEAD_CLAUSE、$ACE_WINDOWS_CAR_GAP_BEHIND_CLAUSE"

/** ACE の手動読み上げで使う前後車ギャップの既定文言。 */
const val ACE_WINDOWS_MANUAL_READOUT_CAR_GAP_TEXT_DEFAULT = ACE_WINDOWS_CAR_GAP_PLACEHOLDERS

/** 前方にギャップがない（0・非有限）ときに前方の節全体へ入れる文言。 */
const val ACE_WINDOWS_CAR_GAP_AHEAD_NONE_TEXT = "前方車なし"

/** 後方にギャップがない（0・非有限）ときに後方の節全体へ入れる文言。 */
const val ACE_WINDOWS_CAR_GAP_BEHIND_NONE_TEXT = "後方車なし"

/** 節を含まない自由文言で、車なし時にプレースホルダー単体へ入れる文言。 */
const val ACE_WINDOWS_CAR_GAP_NONE_VALUE_TEXT = "なし"
