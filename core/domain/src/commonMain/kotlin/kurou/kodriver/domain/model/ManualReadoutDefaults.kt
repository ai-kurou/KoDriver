package kurou.kodriver.domain.model

/** 手動読み上げ一覧で再生ボタン特化表示を使うかどうかの既定値。 */
public const val MANUAL_READOUT_PLAY_BUTTON_LAYOUT_ENABLED_DEFAULT: Boolean = false

/** 手動読み上げの対象項目がない場合の固定文言。 */
public const val MANUAL_READOUT_NO_ITEMS_TEXT: String = "読み上げる項目がありません"

/** 手動読み上げ項目の現在値が取得できない場合の固定文言。 */
public const val MANUAL_READOUT_NO_DATA_TEXT: String = "データがありません"

/** 1セットに追加できる手動読み上げ項目数の上限。 */
public const val MANUAL_READOUT_SET_ITEM_MAX: Int = 3

/** 手動読み上げセット名の最大文字数。 */
public const val MANUAL_READOUT_SET_NAME_MAX_LENGTH: Int = 20

/** セット名から改行を除去し、上限文字数に切り詰める。サロゲートペアを途中で分割しない。 */
public fun String.takeManualReadoutSetName(): String {
    val taken = filterNot { it == '\n' || it == '\r' }.take(MANUAL_READOUT_SET_NAME_MAX_LENGTH)
    return if (taken.lastOrNull()?.isHighSurrogate() == true) taken.dropLast(1) else taken
}
