package kurou.kodriver.feature.otheroverlaytextsizedetail

import kurou.kodriver.domain.model.OverlayTextSize

internal data class OtherOverlayTextSizeDetailUiState(
    // 保存値またはプレビュー値からなる、オーバーレイに適用中のサイズ。
    val selectedOverlayTextSize: OverlayTextSize = OverlayTextSize.MEDIUM,
    val pendingOverlayTextSize: OverlayTextSize = OverlayTextSize.MEDIUM,
)
