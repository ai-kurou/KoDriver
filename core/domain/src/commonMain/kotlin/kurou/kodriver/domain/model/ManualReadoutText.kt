package kurou.kodriver.domain.model

/** 手動読み上げセットの項目文言の区切り文字。 */
private const val MANUAL_READOUT_TEXT_SEPARATOR = "、"

/** 対応項目の文言を「、」で連結する。対応項目がない場合は [MANUAL_READOUT_NO_ITEMS_TEXT]、空文言の項目は除外する。 */
fun joinManualReadoutTexts(texts: List<String>): String =
    if (texts.isEmpty()) {
        MANUAL_READOUT_NO_ITEMS_TEXT
    } else {
        texts.filter { it.isNotBlank() }.joinToString(MANUAL_READOUT_TEXT_SEPARATOR)
    }

/** 項目の現在値が取得できない場合の読み上げ文言（例: 「残り燃料はデータがありません」）。 */
fun manualReadoutNoDataText(itemName: String): String = "${itemName}は$MANUAL_READOUT_NO_DATA_TEXT"
