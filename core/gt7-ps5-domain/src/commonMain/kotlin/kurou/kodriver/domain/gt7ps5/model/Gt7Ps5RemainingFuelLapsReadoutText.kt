package kurou.kodriver.domain.gt7ps5.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** 燃料残り周回数の読み上げ文言の周回数プレースホルダーを置換する。 */
fun formatGt7Ps5RemainingFuelLapsReadoutText(
    template: String,
    laps: Int,
): String = SinglePlaceholderTemplate(GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER).format(template, laps.toString())

/** 文言内の `{...}` のうち、`{laps}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER).findUnknown(template)
