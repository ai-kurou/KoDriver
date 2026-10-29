package kurou.kodriver.feature.othervolumedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge

/** 音量詳細画面で表示する状態を合成する冷たい Flow。共有・初期値は ViewModel が管理する。 */
internal class OtherVolumeDetailFlow(
    soundVolume: Flow<Int>,
    deviceVolumeRefreshTrigger: Flow<Int>,
    getDeviceVolume: suspend () -> Int,
    isPreviewing: Flow<Boolean>,
    lowVolumeWarningEnabled: Flow<Boolean>,
) {
    // ハードウェアボタンやOSの音量UIからの変更も、購読中は一定間隔で再取得する。
    private val deviceVolumePollingTicker: Flow<Unit> =
        flow {
            while (true) {
                delay(DEVICE_VOLUME_POLLING_INTERVAL_MS)
                emit(Unit)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val deviceVolume: Flow<Int> =
        merge(deviceVolumeRefreshTrigger.map { }, deviceVolumePollingTicker)
            .mapLatest { getDeviceVolume() }

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

    private companion object {
        const val DEVICE_VOLUME_POLLING_INTERVAL_MS = 500L
    }
}
