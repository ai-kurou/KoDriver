package kurou.kodriver.domain.model

/** ピットタイミング文言の周回数プレースホルダーを置換する。 */
fun formatLmuWindowsPitTimingVirtualEnergyReadoutText(
    template: String,
    laps: Int,
): String = template.replace(LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER, laps.toString())
