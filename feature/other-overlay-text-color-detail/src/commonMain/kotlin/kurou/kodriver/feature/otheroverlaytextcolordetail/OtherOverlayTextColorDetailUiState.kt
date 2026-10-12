package kurou.kodriver.feature.otheroverlaytextcolordetail

import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT

internal data class OtherOverlayTextColorDetailUiState(
    val selectedOverlayTextColor: Long = OVERLAY_TEXT_COLOR_DEFAULT,
    val pendingOverlayTextColor: Long = OVERLAY_TEXT_COLOR_DEFAULT,
)
