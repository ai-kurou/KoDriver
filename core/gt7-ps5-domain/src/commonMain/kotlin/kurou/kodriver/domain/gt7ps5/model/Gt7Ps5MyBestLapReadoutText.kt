package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate
import kurou.kodriver.domain.util.formatLapTimeReadoutMs

/** 自己ベストラップ更新文言のラップタイムプレースホルダーを置換する。 */
public fun formatGt7Ps5MyBestLapReadoutText(
    template: String,
    lapTimeMs: Int,
): String {
    val lapTime = formatLapTimeReadoutMs(lapTimeMs.toLong())
    return SinglePlaceholderTemplate(GT7_PS5_MY_BEST_LAP_LAPTIME_PLACEHOLDER).format(template, lapTime)
}

/** 文言内の `{...}` のうち、`{laptime}` 以外のトークンを出現順・重複なしで返す。 */
public fun findUnknownGt7Ps5MyBestLapReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(GT7_PS5_MY_BEST_LAP_LAPTIME_PLACEHOLDER).findUnknown(template)
