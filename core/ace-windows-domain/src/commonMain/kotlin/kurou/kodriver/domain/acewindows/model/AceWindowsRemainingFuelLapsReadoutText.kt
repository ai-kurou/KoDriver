package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** 燃料残り周回数の読み上げ文言の周回数プレースホルダーを置換する。 */
fun formatAceWindowsRemainingFuelLapsReadoutText(
    template: String,
    laps: Int,
): String = SinglePlaceholderTemplate(ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER).format(template, laps.toString())

/** 文言内の `{...}` のうち、`{laps}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER).findUnknown(template)
