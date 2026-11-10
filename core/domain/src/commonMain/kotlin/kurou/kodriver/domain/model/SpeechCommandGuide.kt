package kurou.kodriver.domain.model

/**
 * 音声タブの一覧に出す、読み上げ項目1つ分の案内。
 *
 * [key] は手動読み上げ項目のキー、[name] は表示名、[phrases] はコドライバーへ話しかけたときにその項目を読み上げさせる語句
 * （認識結果の表記の揺れに備えた言い方をすべて含む）。
 */
data class SpeechCommandGuideItem(
    val key: String,
    val name: String,
    val phrases: List<String>,
)

/** 音声タブの一覧で、同じ [category] の項目をまとめた1つの区切り。 */
data class SpeechCommandGuideGroup(
    val category: ManualReadoutItemCategory,
    val items: List<SpeechCommandGuideItem>,
)
