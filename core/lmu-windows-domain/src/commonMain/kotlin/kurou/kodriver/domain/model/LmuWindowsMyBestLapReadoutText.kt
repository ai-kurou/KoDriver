package kurou.kodriver.domain.model

/** 自己ベストラップ更新文言のラップタイムプレースホルダーを置換する。 */
fun formatLmuWindowsMyBestLapReadoutText(
    template: String,
    lapTimeMs: Long,
): String {
    val minutes = lapTimeMs / 60_000
    val seconds = lapTimeMs / 1_000 % 60
    val milliseconds = (lapTimeMs % 1_000).toString().padStart(3, '0')
    val lapTime = if (minutes > 0) "${minutes}分${seconds}秒$milliseconds" else "${seconds}秒$milliseconds"
    return template.replace(LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER, lapTime)
}

/** 文言内の `{...}` のうち、`{laptime}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsMyBestLapReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER }
        .distinct()
        .toList()
