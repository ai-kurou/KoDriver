package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT

/**
 * OtherOverlayBackgroundOpacityDetail 画面の表示状態。
 */
data class OtherOverlayBackgroundOpacityDetailUiState(
    val opacity: Int = OVERLAY_BACKGROUND_OPACITY_DEFAULT,
)
