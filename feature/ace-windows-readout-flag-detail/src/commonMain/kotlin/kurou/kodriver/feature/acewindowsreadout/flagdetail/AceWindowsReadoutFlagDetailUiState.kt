package kurou.kodriver.feature.acewindowsreadout.flagdetail

import kurou.kodriver.core.model.ReadoutItemKey

internal data class AceWindowsReadoutFlagDetailUiState(
    val enabledStates: Map<ReadoutItemKey, Boolean> = emptyMap(),
)
