package kurou.kodriver.domain.model

/** ピットタイミング文言の周回数プレースホルダーを置換する。 */
fun formatLmuWindowsPitTimingReadoutText(
    template: String,
    laps: Int,
): String = template.replace(LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER, laps.toString())

/** 文言内の `{...}` のうち、`{laps}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsPitTimingReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER }
        .distinct()
        .toList()
