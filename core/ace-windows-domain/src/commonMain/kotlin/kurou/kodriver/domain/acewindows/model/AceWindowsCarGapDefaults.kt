package kurou.kodriver.domain.acewindows.model

/** 前後車ギャップ文言の前方プレースホルダー。「前方1.5秒」または「前方車なし」に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER = "{ahead}"

/** 前後車ギャップ文言の後方プレースホルダー。「後方2.3秒」または「後方車なし」に置き換わる。 */
const val ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER = "{behind}"

/** 編集画面で挿入する前方・後方プレースホルダーの組。 */
const val ACE_WINDOWS_CAR_GAP_PLACEHOLDERS =
    "$ACE_WINDOWS_CAR_GAP_AHEAD_PLACEHOLDER、$ACE_WINDOWS_CAR_GAP_BEHIND_PLACEHOLDER"

/** ACE の手動読み上げで使う前後車ギャップの既定文言。 */
const val ACE_WINDOWS_MANUAL_READOUT_CAR_GAP_TEXT_DEFAULT = ACE_WINDOWS_CAR_GAP_PLACEHOLDERS

/** 前方にギャップがない（0・非有限）ときに前方プレースホルダーへ入れる文言。 */
const val ACE_WINDOWS_CAR_GAP_AHEAD_NONE_TEXT = "前方車なし"

/** 後方にギャップがない（0・非有限）ときに後方プレースホルダーへ入れる文言。 */
const val ACE_WINDOWS_CAR_GAP_BEHIND_NONE_TEXT = "後方車なし"
