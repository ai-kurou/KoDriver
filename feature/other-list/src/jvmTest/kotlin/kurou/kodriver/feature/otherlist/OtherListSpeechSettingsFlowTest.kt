package kurou.kodriver.feature.otherlist

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.SOUND_VOLUME_DEFAULT
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveAndroidAudioUsageUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutStartSoundTypeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveVoicePitchUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceSpeedUseCase
import kurou.kodriver.domain.usecase.ObserveVoiceUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListSpeechSettingsFlowTest {
    private val voiceRepository: VoicePreferencesRepository = mockk()
    private val voiceSpeedRepository: VoiceSpeedPreferencesRepository = mockk()
    private val voicePitchRepository: VoicePitchPreferencesRepository = mockk()
    private val audioUsageRepository: AndroidAudioUsagePreferencesRepository = mockk()
    private val startSoundRepository: ReadoutStartSoundPreferencesRepository = mockk()
    private val soundVolumeRepository: SoundVolumePreferencesRepository = mockk()

    @Test
    fun `初期状態と全設定の更新を状態に反映する`() =
        runTest {
            val voice = MutableStateFlow(VOICE_ID_UNSPECIFIED)
            val speed = MutableStateFlow(VOICE_SPEED_DEFAULT)
            val pitch = MutableStateFlow(VOICE_PITCH_DEFAULT)
            val usage = MutableStateFlow(AndroidAudioUsage.MEDIA)
            val startSound = MutableStateFlow(READOUT_START_SOUND_TYPE_DEFAULT)
            val volume = MutableStateFlow(SOUND_VOLUME_DEFAULT)
            every { voiceRepository.voiceId() } returns voice
            every { voiceSpeedRepository.voiceSpeed() } returns speed
            every { voicePitchRepository.voicePitch() } returns pitch
            every { audioUsageRepository.observeAndroidAudioUsage() } returns usage
            every { startSoundRepository.observeType() } returns startSound
            every { soundVolumeRepository.volume() } returns volume
            val flow = createFlow()

            assertEquals(
                OtherListSpeechSettingsUiState(),
                flow.uiState.first(),
            )
            val updatedState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first {
                        it ==
                            OtherListSpeechSettingsUiState(
                                "voice-id",
                                1.5f,
                                0.8f,
                                AndroidAudioUsage.COMMUNICATION,
                                ReadoutStartSoundType.ELECTRONIC_NOISE,
                                42,
                            )
                    }
                }

            voice.value = "voice-id"
            speed.value = 1.5f
            pitch.value = 0.8f
            usage.value = AndroidAudioUsage.COMMUNICATION
            startSound.value = ReadoutStartSoundType.ELECTRONIC_NOISE
            volume.value = 42

            assertEquals(
                OtherListSpeechSettingsUiState(
                    "voice-id",
                    1.5f,
                    0.8f,
                    AndroidAudioUsage.COMMUNICATION,
                    ReadoutStartSoundType.ELECTRONIC_NOISE,
                    42,
                ),
                updatedState.await(),
            )
            verify(exactly = 1) { voiceRepository.voiceId() }
            verify(exactly = 1) { voiceSpeedRepository.voiceSpeed() }
            verify(exactly = 1) { voicePitchRepository.voicePitch() }
            verify(exactly = 1) { audioUsageRepository.observeAndroidAudioUsage() }
            verify(exactly = 1) { startSoundRepository.observeType() }
            verify(exactly = 1) { soundVolumeRepository.volume() }
            confirmVerified(
                voiceRepository,
                voiceSpeedRepository,
                voicePitchRepository,
                audioUsageRepository,
                startSoundRepository,
                soundVolumeRepository,
            )
        }

    private fun createFlow() =
        OtherListSpeechSettingsFlow(
            ObserveVoiceUseCase(voiceRepository),
            ObserveVoiceSpeedUseCase(voiceSpeedRepository),
            ObserveVoicePitchUseCase(voicePitchRepository),
            ObserveAndroidAudioUsageUseCase(audioUsageRepository),
            ObserveReadoutStartSoundTypeUseCase(startSoundRepository),
            ObserveSoundVolumeUseCase(soundVolumeRepository),
        )
}
