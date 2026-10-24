package kurou.kodriver.domain.lmuwindows.model

fun formatLmuWindowsRemainingFuelReadoutText(
    template: String,
    percentage: Int,
): String = template.replace(LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER, percentage.toString())

fun findUnknownLmuWindowsRemainingFuelReadoutPlaceholders(template: String): List<String> =
    Regex("\\{[^{}]*\\}")
        .findAll(template)
        .map { it.value }
        .filter { it != LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER }
        .distinct()
        .toList()
