package kurou.kodriver.feature.otheroverlaytextsizedetail

import kurou.kodriver.domain.model.OverlayTextSize

internal data class OtherOverlayTextSizeDetailUiState(
    val selectedOverlayTextSize: OverlayTextSize = OverlayTextSize.MEDIUM,
    val pendingOverlayTextSize: OverlayTextSize = OverlayTextSize.MEDIUM,
)
