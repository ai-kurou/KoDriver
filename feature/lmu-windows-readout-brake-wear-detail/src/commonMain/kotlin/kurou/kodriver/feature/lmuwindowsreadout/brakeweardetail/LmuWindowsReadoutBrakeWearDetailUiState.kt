package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData

internal data class LmuWindowsReadoutBrakeWearDetailUiState(
    val current: LmuWindowsBrakeWearInvestigationData = LmuWindowsBrakeWearInvestigationData(),
    val baseline: LmuWindowsBrakeWearInvestigationData? = null,
) {
    /** どちらかの値を取得できているか。取得できていない値は基準にできない。 */
    val hasCurrentValues: Boolean
        get() = current.wearablesBrakes != null || current.brakeInfo != null
}
