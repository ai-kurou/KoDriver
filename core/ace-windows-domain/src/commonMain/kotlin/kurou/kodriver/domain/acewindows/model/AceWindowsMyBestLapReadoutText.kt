package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate
import kurou.kodriver.domain.util.formatLapTimeReadoutMs

/** 自己ベストラップ更新文言のラップタイムプレースホルダーを置換する。 */
public fun formatAceWindowsMyBestLapReadoutText(
    template: String,
    lapTimeMs: Int,
): String {
    val lapTime = formatLapTimeReadoutMs(lapTimeMs.toLong())
    return SinglePlaceholderTemplate(ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER).format(template, lapTime)
}

/** 文言内の `{...}` のうち、`{laptime}` 以外のトークンを出現順・重複なしで返す。 */
public fun findUnknownAceWindowsMyBestLapReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER).findUnknown(template)
