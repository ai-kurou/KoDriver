package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.SinglePlaceholderTemplate

public fun formatLmuWindowsRemainingFuelReadoutText(
    template: String,
    percentage: Int,
): String =
    SinglePlaceholderTemplate(LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER).format(template, percentage.toString())

public fun findUnknownLmuWindowsRemainingFuelReadoutPlaceholders(template: String): List<String> =
    SinglePlaceholderTemplate(LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER).findUnknown(template)
