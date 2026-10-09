package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData

internal data class LmuWindowsReadoutBrakeWearDetailUiState(
    val current: LmuWindowsBrakeWearInvestigationData = LmuWindowsBrakeWearInvestigationData(),
    val baseline: LmuWindowsBrakeWearInvestigationData? = null,
)
