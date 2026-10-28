package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kurou.kodriver.domain.model.LowVolumeWarning

/** 音量警告の表示状態を生成する冷たい Flow。閉じた状態と購読の寿命は呼び出し元が管理する。 */
internal class LowVolumeWarningFlow(
    warning: Flow<LowVolumeWarning?>,
    dismissed: Flow<Boolean>,
    resetDismissal: () -> Unit,
) {
    val uiState: Flow<LowVolumeWarningUiState> =
        warning
            .onEach { if (it == null) resetDismissal() }
            .combine(dismissed) { warning, isDismissed ->
                if (warning == null || isDismissed) {
                    LowVolumeWarningUiState()
                } else {
                    LowVolumeWarningUiState(warning.deviceVolume, warning.soundVolume)
                }
            }
}
