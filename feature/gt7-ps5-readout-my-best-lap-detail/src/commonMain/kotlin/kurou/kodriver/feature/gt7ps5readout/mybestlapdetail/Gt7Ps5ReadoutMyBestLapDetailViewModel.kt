package kurou.kodriver.feature.gt7ps5readout.mybestlapdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.model.readoutEnabled
import kurou.kodriver.domain.preview.ReadoutTextPreviewHelper
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.SaveReadoutEnabledStateUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase

/**
 * GT7 自己ベストラップアナウンス詳細設定の ViewModel。
 *
 * 自己ベストラップ更新の有効/無効・文言はいずれも DataStore に永続化される。
 */
internal data class MyBestLapUseCases(
    val observeEnabledStates: ObserveReadoutEnabledStatesUseCase,
    val saveEnabledState: SaveReadoutEnabledStateUseCase,
)

internal data class MyBestLapReadoutUseCases(
    val observeText: ObserveGt7Ps5MyBestLapReadoutTextUseCase,
    val saveText: SaveGt7Ps5MyBestLapReadoutTextUseCase,
    val speakText: SpeakTextUseCase,
    val playStartSoundForKey: PlayStartSoundForKeyUseCase,
    val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase,
    val observeSoundVolume: ObserveSoundVolumeUseCase,
)

internal class Gt7Ps5ReadoutMyBestLapDetailViewModel(
    private val myBestLapUseCases: MyBestLapUseCases,
    private val readout: MyBestLapReadoutUseCases,
) : ViewModel() {
    private val preview =
        ReadoutTextPreviewHelper(
            viewModelScope,
            readout.checkTextToSpeechAvailable,
            readout.observeSoundVolume,
            readout.playStartSoundForKey,
            readout.speakText,
        )

    val uiState: StateFlow<Gt7Ps5ReadoutMyBestLapDetailUiState> =
        combine(
            myBestLapUseCases.observeEnabledStates(Simulator.Gt7Ps5.id),
            readout.observeText(),
            preview.textToSpeechAvailable,
        ) { states, text, available ->
            Gt7Ps5ReadoutMyBestLapDetailUiState(
                enabled = states.readoutEnabled(ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled),
                readoutText = text,
                isTextToSpeechAvailable = available,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            Gt7Ps5ReadoutMyBestLapDetailUiState(),
        )

    /** ペインを離れるときに試聴を止める。 */
    fun onPreviewStopped() = preview.stop()

    fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            myBestLapUseCases.saveEnabledState(
                Simulator.Gt7Ps5.id,
                ReadoutItemKey.Gt7Ps5.MyBestLap.DetailEnabled,
                enabled,
            )
        }
    }

    fun onReadoutTextChanged(text: String) {
        viewModelScope.launch { readout.saveText(text) }
    }

    /** サンプルタイムに置換し、空白文言・TTS利用不可・音量ゼロでは再生しない。 */
    fun onReadoutTextPreviewClicked(text: String) {
        val formattedText = formatGt7Ps5MyBestLapReadoutText(text, 83_456)
        previewText(formattedText)
    }

    private fun previewText(text: String) {
        preview.onPreviewClicked(text, ReadoutItemKey.Gt7Ps5.MyBestLap.Root)
    }
}
