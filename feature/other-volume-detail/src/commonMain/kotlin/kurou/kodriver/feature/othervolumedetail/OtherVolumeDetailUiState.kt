package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.model.LOW_VOLUME_WARNING_ENABLED_DEFAULT

/**
 * OtherVolumeDetail 画面の表示状態。
 */
data class OtherVolumeDetailUiState(
    val volume: Int = 100,
    val deviceVolume: Int = DEVICE_VOLUME_MIN,
    val isPreviewing: Boolean = false,
    val lowVolumeWarningEnabled: Boolean = LOW_VOLUME_WARNING_ENABLED_DEFAULT,
)
