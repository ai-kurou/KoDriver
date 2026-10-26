package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase

/** 読み上げ設定の観測値を表示状態へ集約する。実行のライフサイクルは呼び出し元に従う。 */
class OtherListSpeechSettingsFlow(
    private val observeVoice: ObserveVoiceUseCase,
    private val observeVoiceSpeed: ObserveVoiceSpeedUseCase,
    private val observeVoicePitch: ObserveVoicePitchUseCase,
    private val observeAndroidAudioUsage: ObserveAndroidAudioUsageUseCase,
    private val observeReadoutStartSoundType: ObserveReadoutStartSoundTypeUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) {
    val uiState: Flow<OtherListSpeechSettingsUiState> =
        combine(
            combine(
                observeVoice(),
                observeVoiceSpeed(),
                observeVoicePitch(),
                observeAndroidAudioUsage(),
                observeReadoutStartSoundType(),
            ) { voiceId, voiceSpeed, voicePitch, androidAudioUsage, readoutStartSoundType ->
                OtherListSpeechSettingsUiState(
                    voiceId,
                    voiceSpeed,
                    voicePitch,
                    androidAudioUsage,
                    readoutStartSoundType,
                )
            },
            observeSoundVolume(),
        ) { state, soundVolume -> state.copy(soundVolume = soundVolume) }
}
