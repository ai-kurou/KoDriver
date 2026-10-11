package kurou.kodriver.feature.othervolumedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
import kurou.kodriver.domain.engine.LmuWindowsMyBestLap
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

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
    private val speakText: SpeakTextUseCase,
) : ViewModel() {
    private val isPreviewing = MutableStateFlow(false)
    private var previewJob: Job? = null
    private var saveJob: Job? = null
    private var previewRequest = 0

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
            isPreviewing,
        ) { volume, deviceVolume, previewing ->
            OtherVolumeDetailUiState(volume = volume, deviceVolume = deviceVolume, isPreviewing = previewing)
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
        saveJob = viewModelScope.launch { soundVolumeUseCases.saveSoundVolume(volume) }
    }

    /** ペインを離れたときに、再生中の試聴を止める。 */
    fun onPreviewStopped() {
        previewRequest++
        previewJob?.cancel()
        isPreviewing.update { false }
    }

    /** 試聴中は停止し、停止中は保存済みの音量・音声・速度で既定文言を読み上げる。 */
    fun onPreviewClicked() {
        val stopPreview = isPreviewing.value
        val request = ++previewRequest
        previewJob?.cancel()
        isPreviewing.update { false }
        if (stopPreview) return
        // 保存・音量取得待ちも停止できるよう、要求した時点から試聴中として扱う。
        isPreviewing.update { true }
        previewJob =
            viewModelScope.launch {
                try {
                    // 直前のスライダー操作の保存が終わってから、最新の音量で試聴する。
                    saveJob?.join()
                    val volume = soundVolumeUseCases.observeSoundVolume().first()
                    if (volume <= 0) return@launch
                    // 自己ベスト文言が空欄でも、従来の既定文言で音量を確認できるようにする。
                    val sample = LmuWindowsMyBestLap(lapTimeMs = PREVIEW_LAP_TIME_MS)
                    speakText(sample.narratedText, volume = volume)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // 試聴に失敗しても画面の操作を続けられるようにする。
                } finally {
                    finishPreview(request)
                }
            }
    }

    // 古い試聴の終了で新しい試聴状態を解除しない。
    private fun finishPreview(request: Int) {
        if (previewRequest == request) isPreviewing.update { false }
    }

    fun onDeviceVolumeChanged(volume: Int) {
        deviceVolumeRequest.trySend(volume)
    }

    private companion object {
        const val DEVICE_VOLUME_POLLING_INTERVAL_MS = 500L
        const val PREVIEW_LAP_TIME_MS = 83_456L
    }
}
