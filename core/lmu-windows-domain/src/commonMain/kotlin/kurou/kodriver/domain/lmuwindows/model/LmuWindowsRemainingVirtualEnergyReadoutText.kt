package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

/** バーチャルエネルギー残量警告文言の閾値プレースホルダーを置換する。 */
fun formatLmuWindowsRemainingVirtualEnergyReadoutText(
    template: String,
    percentage: Int,
): String =
    SinglePlaceholderTemplate(LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER).format(
        template,
        percentage.toString(),
    )

/** 文言内の `{...}` のうち、`{percent}` 以外のトークンを出現順・重複なしで返す。 */
fun findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER).findUnknown(template)
