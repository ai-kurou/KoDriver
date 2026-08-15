package kurou.kodriver.feature.othervolumedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase

internal class OtherVolumeDetailViewModel(
    observeSoundVolume: ObserveSoundVolumeUseCase,
    private val saveSoundVolume: SaveSoundVolumeUseCase,
    private val getDeviceVolume: GetDeviceVolumeUseCase,
    private val setDeviceVolume: SetDeviceVolumeUseCase,
) : ViewModel() {
    private val deviceVolumeRefreshTrigger = MutableStateFlow(0)

    // 連続でスライダーを操作した場合でも書き込みが逆順に完了してOS音量が古い値のまま
    // 残らないよう、要求された音量はMutableStateFlowに集約し単一のコルーチンで直列に処理する。
    // MutableStateFlowは常に最新値のみを保持するため、処理中に複数回更新されても
    // 直前の書き込み完了後に最新の要求値のみが1回だけ書き込まれる。
    private val deviceVolumeRequest = MutableStateFlow<Int?>(null)

    // ハードウェアボタンやOSの音量UIなど、KoDriver外から端末のマスター音量が変更された場合にも
    // detailPane表示中はスライダーへ反映されるよう、一定間隔で再取得する。
    private val deviceVolumePollingTicker: Flow<Unit> =
        flow {
            while (true) {
                delay(DEVICE_VOLUME_POLLING_INTERVAL_MS)
                emit(Unit)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OtherVolumeDetailUiState> =
        combine(
            observeSoundVolume(),
            merge(deviceVolumeRefreshTrigger.map { }, deviceVolumePollingTicker)
                .mapLatest { getDeviceVolume() },
        ) { volume, deviceVolume ->
            OtherVolumeDetailUiState(volume = volume, deviceVolume = deviceVolume)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVolumeDetailUiState())

    @Suppress("UnusedPrivateProperty")
    private val deviceVolumeWriterJob =
        deviceVolumeRequest
            .filterNotNull()
            .onEach { volume ->
                setDeviceVolume(volume)
                deviceVolumeRefreshTrigger.update { it + 1 }
            }.launchIn(viewModelScope)

    fun onVolumeChanged(volume: Int) {
        viewModelScope.launch { saveSoundVolume(volume) }
    }

    fun onDeviceVolumeChanged(volume: Int) {
        deviceVolumeRequest.update { volume }
    }

    private companion object {
        const val DEVICE_VOLUME_POLLING_INTERVAL_MS = 500L
    }
}
