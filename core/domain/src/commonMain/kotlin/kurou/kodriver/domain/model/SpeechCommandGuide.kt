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

/**
 * この手動読み上げ spec の項目定義と語句から、音声タブの案内をカテゴリ順に組み立てる。語句を持たない項目は含めない。
 * 語句は音声認識の照合と同じ [ManualReadoutSimulatorSpec.itemPhrases] なので、一覧に出る言い方と実際に反応する言い方は一致する。
 */
fun ManualReadoutSimulatorSpec.toSpeechCommandGuideGroups(): List<SpeechCommandGuideGroup> {
    val items =
        itemDefinitions.values.mapNotNull { definition ->
            val phrases = itemPhrases[definition.key].orEmpty()
            if (phrases.isEmpty()) return@mapNotNull null
            definition.category to SpeechCommandGuideItem(definition.key, definition.name, phrases)
        }
    return ManualReadoutItemCategory.entries.mapNotNull { category ->
        val categoryItems = items.filter { it.first == category }.map { it.second }
        if (categoryItems.isEmpty()) null else SpeechCommandGuideGroup(category, categoryItems)
    }
}
