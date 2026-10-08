package kurou.kodriver.feature.othervolumedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlaySpeechEventUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase

internal data class SoundVolumeUseCases(
    val observeSoundVolume: ObserveSoundVolumeUseCase,
    val saveSoundVolume: SaveSoundVolumeUseCase,
)

internal data class DeviceVolumeUseCases(
    val getDeviceVolume: GetDeviceVolumeUseCase,
    val setDeviceVolume: SetDeviceVolumeUseCase,
)

internal class OtherVolumeDetailViewModel(
    private val soundVolumeUseCases: SoundVolumeUseCases,
    private val deviceVolumeUseCases: DeviceVolumeUseCases,
    private val playSpeechEvent: PlaySpeechEventUseCase,
) : ViewModel() {
    private val deviceVolumeRefreshTrigger = MutableStateFlow(0)

    // 連続でスライダーを操作した場合でも書き込みが逆順に完了してOS音量が古い値のまま
    // 残らないよう、要求された音量はChannelに集約し単一のコルーチンで直列に処理する。
    // Channel.CONFLATEDは未処理の最新値のみを保持するため、処理中に複数回要求されても
    // 直前の書き込み完了後に最新の要求値のみが1回だけ書き込まれる。
    // 前回と同じ値の再要求も処理され、OS側で変更された音量を再設定できる。
    private val deviceVolumeRequest = Channel<Int>(Channel.CONFLATED)

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
            soundVolumeUseCases.observeSoundVolume(),
            merge(deviceVolumeRefreshTrigger.map { }, deviceVolumePollingTicker)
                .mapLatest { deviceVolumeUseCases.getDeviceVolume() },
        ) { volume, deviceVolume ->
            OtherVolumeDetailUiState(volume = volume, deviceVolume = deviceVolume)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherVolumeDetailUiState())

    @Suppress("UnusedPrivateProperty")
    private val deviceVolumeWriterJob =
        deviceVolumeRequest
            .receiveAsFlow()
            .onEach { volume ->
                deviceVolumeUseCases.setDeviceVolume(volume)
                deviceVolumeRefreshTrigger.update { it + 1 }
            }.launchIn(viewModelScope)

    fun onVolumeChanged(volume: Int) {
        viewModelScope.launch { soundVolumeUseCases.saveSoundVolume(volume) }
    }

    fun onPreviewClicked() {
        // 保存済みの自己ベスト文言が空欄でも音量を確認できるよう、既定文言を解決済みとして再生する。
        val sample = SpeechEvent.LmuWindowsMyBestLap(lapTimeMs = PREVIEW_LAP_TIME_MS)
        playSpeechEvent(sample.copy(resolvedText = sample.narratedText))
    }

    fun onDeviceVolumeChanged(volume: Int) {
        deviceVolumeRequest.trySend(volume)
    }

    private companion object {
        const val DEVICE_VOLUME_POLLING_INTERVAL_MS = 500L
        const val PREVIEW_LAP_TIME_MS = 83_456L
    }
}
