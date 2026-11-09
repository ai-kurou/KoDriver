package kurou.kodriver.domain.util

/**
 * 音声認識の確定結果の候補から、手動読み上げ項目のキーを選ぶ。[phrasesByItemKey] は項目キーごとの語句。
 * すべての候補を調べ、語句に部分一致したもののうち一致した語句が最も長い項目を選ぶ。
 * 第1候補が「燃料」のように語尾を取りこぼしていても、下位候補の「燃料周回」のような、より具体的な語句を優先するため。
 * 一致した語句の長さが同じ場合は、信頼度の高い（先頭に近い）候補を採用する。
 * 空白は無視し、英字は大文字小文字を区別しない。英字だけの語句（例: VE）は、前後が英字でない場合だけ一致とする（have の中の ve に反応しない）。
 * どの項目にも一致しなければ null。
 */
fun matchManualReadoutItemKey(
    candidates: List<String>,
    phrasesByItemKey: Map<String, List<String>>,
): String? {
    var bestKey: String? = null
    var bestLength = 0
    for (candidate in candidates) {
        val normalized = normalizeSpokenText(candidate)
        if (normalized.isEmpty()) continue
        for ((key, phrases) in phrasesByItemKey) {
            val length =
                phrases
                    .map(::normalizeSpokenText)
                    .filter { it.isNotEmpty() && containsPhrase(normalized, it) }
                    .maxOfOrNull { it.length }
                    ?: continue
            if (length > bestLength) {
                bestKey = key
                bestLength = length
            }
        }
    }
    return bestKey
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
