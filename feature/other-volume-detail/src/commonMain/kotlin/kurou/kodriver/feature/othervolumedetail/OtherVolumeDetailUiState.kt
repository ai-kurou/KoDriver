package kurou.kodriver.feature.othervolumedetail

import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN

/**
 * OtherVolumeDetail 画面の表示状態。
 */
data class OtherVolumeDetailUiState(
    val volume: Int = 100,
    val deviceVolume: Int = DEVICE_VOLUME_MIN,
)
