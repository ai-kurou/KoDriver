package kurou.kodriver.domain.lmuwindows.model

/**
 * 音声認識の確定結果の候補から、手動読み上げ項目のキーを選ぶ。
 * 候補は信頼度の高い順に見て、語句に部分一致した最初の候補を採用する。
 * 1つの候補が複数項目の語句に一致する場合は、一致した語句が最も長い項目を選ぶ。
 * 空白は無視し、英字は大文字小文字を区別しない。どの項目にも一致しなければ null。
 */
fun matchLmuWindowsManualReadoutItemKey(candidates: List<String>): String? {
    for (candidate in candidates) {
        val normalized = normalizeSpokenText(candidate)
        if (normalized.isEmpty()) continue
        val matched =
            lmuWindowsManualReadoutItemPhrases
                .map { (key, phrases) ->
                    key to
                        phrases
                            .map(::normalizeSpokenText)
                            .filter { it.isNotEmpty() && normalized.contains(it) }
                            .maxOfOrNull { it.length }
                }.filter { it.second != null }
                .maxByOrNull { it.second ?: 0 }
        if (matched != null) return matched.first
    }
    return null
}

private fun normalizeSpokenText(text: String): String = text.filterNot { it.isWhitespace() }.lowercase()
