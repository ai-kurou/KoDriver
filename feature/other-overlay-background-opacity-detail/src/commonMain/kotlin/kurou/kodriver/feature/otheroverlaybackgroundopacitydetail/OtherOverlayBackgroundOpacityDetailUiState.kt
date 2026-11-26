package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import kurou.kodriver.domain.overlay.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT

/**
 * OtherOverlayBackgroundOpacityDetail 画面の表示状態。
 */
public data class OtherOverlayBackgroundOpacityDetailUiState(
    val opacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
)
