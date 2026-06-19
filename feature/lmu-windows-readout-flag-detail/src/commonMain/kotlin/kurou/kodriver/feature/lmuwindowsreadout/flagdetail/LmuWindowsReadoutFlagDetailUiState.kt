package kurou.kodriver.feature.lmuwindowsreadout.flagdetail

import kurou.kodriver.domain.model.ReadoutItemKey

data class LmuWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
)
