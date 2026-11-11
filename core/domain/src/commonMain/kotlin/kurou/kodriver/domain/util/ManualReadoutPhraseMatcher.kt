package kurou.kodriver.domain.util

/**
 * 音声認識の確定結果の候補から、手動読み上げ項目のキーを選ぶ。[phrasesByItemKey] は項目キーごとの語句。
 * 最初に語句へ一致した候補では、部分一致したもののうち一致した語句が最も長い項目を選ぶ。
 * 後続の（信頼度の低い）候補は、一致した語句が現在の最良語句を含み、かつより長い場合だけ採用する。
 * 第1候補が「タイヤ残り」のように語尾を取りこぼしていても、下位候補の「タイヤ残り周回」のような、より具体的な語句を優先するため。
 * 第1候補と無関係な項目の語句は、長くても下位候補では採用しない（信頼度順を保つ）。
 * 一致した語句の長さが同じ場合は、信頼度の高い（先頭に近い）候補・項目を採用する。
 * 空白は無視し、英字は大文字小文字を区別しない。英字だけの語句（例: VE）は、前後が英字でない場合だけ一致とする（have の中の ve に反応しない）。
 * どの項目にも一致しなければ null。
 */
fun matchManualReadoutItemKey(
    candidates: List<String>,
    phrasesByItemKey: Map<String, List<String>>,
): String? {
    var bestKey: String? = null
    var bestPhrase: String? = null
    for (candidate in candidates) {
        val normalized = normalizeSpokenText(candidate)
        if (normalized.isEmpty()) continue
        val current = bestPhrase
        val matched =
            phrasesByItemKey.flatMap { (key, phrases) ->
                phrases
                    .map(::normalizeSpokenText)
                    .filter { it.isNotEmpty() && containsPhrase(normalized, it) }
                    .map { key to it }
            }
        val best =
            matched
                .filter { (_, phrase) -> current == null || isMoreSpecific(phrase, current) }
                .maxByOrNull { (_, phrase) -> phrase.length }
                ?: continue
        bestKey = best.first
        bestPhrase = best.second
    }
    return bestKey
}

private fun isMoreSpecific(
    phrase: String,
    current: String,
): Boolean = phrase.length > current.length && phrase.contains(current)

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
