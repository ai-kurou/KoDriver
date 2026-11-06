package kurou.kodriver.domain.util

/**
 * 音声認識の確定結果の候補から、手動読み上げ項目のキーを選ぶ。[phrasesByItemKey] は項目キーごとの語句。
 * 候補は信頼度の高い順に見て、語句に部分一致した最初の候補を採用する。
 * 1つの候補が複数項目の語句に一致する場合は、一致した語句が最も長い項目を選ぶ。
 * 空白は無視し、英字は大文字小文字を区別しない。英字だけの語句（例: VE）は、前後が英字でない場合だけ一致とする（have の中の ve に反応しない）。
 * どの項目にも一致しなければ null。
 */
fun matchManualReadoutItemKey(
    candidates: List<String>,
    phrasesByItemKey: Map<String, List<String>>,
): String? {
    for (candidate in candidates) {
        val normalized = normalizeSpokenText(candidate)
        if (normalized.isEmpty()) continue
        val matched =
            phrasesByItemKey
                .map { (key, phrases) ->
                    key to
                        phrases
                            .map(::normalizeSpokenText)
                            .filter { it.isNotEmpty() && containsPhrase(normalized, it) }
                            .maxOfOrNull { it.length }
                }.filter { it.second != null }
                .maxByOrNull { it.second ?: 0 }
        if (matched != null) return matched.first
    }
    return null
}

private fun normalizeSpokenText(text: String): String = text.filterNot { it.isWhitespace() }.lowercase()

private fun isAsciiLetter(c: Char): Boolean = c in 'a'..'z'

private fun containsPhrase(
    text: String,
    phrase: String,
): Boolean {
    if (!phrase.all(::isAsciiLetter)) return text.contains(phrase)
    var from = text.indexOf(phrase)
    while (from >= 0) {
        val end = from + phrase.length
        val beforeOk = from == 0 || !isAsciiLetter(text[from - 1])
        val afterOk = end == text.length || !isAsciiLetter(text[end])
        if (beforeOk && afterOk) return true
        from = text.indexOf(phrase, from + 1)
    }
    return false
}
