package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 音量詳細画面で表示する状態を合成する冷たい Flow。共有・初期値は ViewModel が管理する。 */
internal class OtherVolumeDetailFlow(
    soundVolume: Flow<Int>,
    deviceVolume: Flow<Int>,
    isPreviewing: Flow<Boolean>,
    lowVolumeWarningEnabled: Flow<Boolean>,
) {
    val uiState: Flow<OtherVolumeDetailUiState> =
        combine(
            soundVolume,
            deviceVolume,
            isPreviewing,
            lowVolumeWarningEnabled,
        ) { volume, deviceVolume, previewing, warningEnabled ->
            OtherVolumeDetailUiState(
                volume = volume,
                deviceVolume = deviceVolume,
                isPreviewing = previewing,
                lowVolumeWarningEnabled = warningEnabled,
            )
        }
}
