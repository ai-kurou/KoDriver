package kurou.kodriver.domain.model

/** ソースと残り周回数から、ログ用のピットタイミング既定文言を返す。 */
fun defaultLmuWindowsPitTimingReadoutText(
    source: PitTimingSource,
    laps: Int,
): String {
    val (text, imminentText) =
        when (source) {
            PitTimingSource.VirtualEnergy -> {
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT to
                    LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
            }

            PitTimingSource.TyreWear -> {
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT to
                    LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
            }
        }
    return if (laps <= 0) imminentText else formatLmuWindowsPitTimingReadoutText(text, laps)
}

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
