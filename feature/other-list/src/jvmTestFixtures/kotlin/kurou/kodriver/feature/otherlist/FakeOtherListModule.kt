package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.READOUT_START_SOUND_TYPE_DEFAULT
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.TextToSpeechUnavailableReason
import kurou.kodriver.domain.model.VOICE_PITCH_DEFAULT
import kurou.kodriver.domain.model.VOICE_SPEED_DEFAULT
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SpeechSettingsSenderRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import org.koin.dsl.module

/**
 * テスト用の Fake Koin モジュール（testFixtures）。:core:windows-startup-data の代わりに
 * StartupEnabledRepository の Fake 実装をバインドし、実OSのレジストリ操作を避ける。また、
 * HapticFeedbackAvailabilityRepository を常に振動機能ありとするFake実装に、
 * AccessLocalNetworkPermissionRepository を常に許可済みとするFake実装に差し替え、
 * テスト実行環境（実機・エミュレータ）のハードウェア・権限状態にテスト結果が左右されないようにする。
 */
val fakeTextToSpeechRepository = FakeTextToSpeechRepository()

val fakeSpeechSettingsSenderRepository = FakeSpeechSettingsSenderRepository()

val fakeOtherListModule =
    module {
        single<VoiceSpeedPreferencesRepository> { FakeVoiceSpeedPreferencesRepository() }
        single<VoicePitchPreferencesRepository> { FakeVoicePitchPreferencesRepository() }
        single<ReadoutStartSoundPreferencesRepository> { FakeReadoutStartSoundPreferencesRepository() }
        single<StartupEnabledRepository> { FakeStartupEnabledRepository() }
        single<HapticFeedbackAvailabilityRepository> { FakeHapticFeedbackAvailabilityRepository() }
        single<AccessLocalNetworkPermissionRepository> { FakeAccessLocalNetworkPermissionRepository() }
        single<SpeechSettingsSenderRepository> { fakeSpeechSettingsSenderRepository }
        single<TextToSpeechRepository> { fakeTextToSpeechRepository }
    }

class FakeStartupEnabledRepository : StartupEnabledRepository {
    private var enabled = false

    override suspend fun isEnabled(): Boolean = enabled

    override suspend fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }
}

class FakeTextToSpeechRepository : TextToSpeechRepository {
    var unavailableReason: TextToSpeechUnavailableReason = TextToSpeechUnavailableReason.WindowsSpeechUnavailable

    override suspend fun isAvailable(): Boolean = false

    override suspend fun unavailableReason(): TextToSpeechUnavailableReason = unavailableReason

    override suspend fun speak(
        text: String,
        queue: Boolean,
        volume: Int,
        voiceId: String,
        speed: Float,
    ) = Unit

    override suspend fun stop() = Unit
}

class FakeHapticFeedbackAvailabilityRepository : HapticFeedbackAvailabilityRepository {
    var available = true

    override fun isHapticFeedbackAvailable(): Boolean = available
}

class FakeAccessLocalNetworkPermissionRepository : AccessLocalNetworkPermissionRepository {
    var granted = true

    override fun isGranted(): Boolean = granted
}

class FakeSpeechSettingsSenderRepository : SpeechSettingsSenderRepository {
    var openWindowsSpeechSettingsCallCount = 0

    override fun openWindowsSpeechSettings() {
        openWindowsSpeechSettingsCallCount++
    }
}

class FakeReadoutStartSoundPreferencesRepository : ReadoutStartSoundPreferencesRepository {
    private val type = MutableStateFlow(READOUT_START_SOUND_TYPE_DEFAULT)

    override fun observeType() = type

    override suspend fun saveType(type: ReadoutStartSoundType) {
        this.type.update { type }
    }
}

class FakeVoiceSpeedPreferencesRepository : VoiceSpeedPreferencesRepository {
    private val speed = MutableStateFlow(VOICE_SPEED_DEFAULT)

    override fun voiceSpeed() = speed

    override suspend fun saveVoiceSpeed(voiceSpeed: Float) {
        speed.update { voiceSpeed }
    }
}

class FakeVoicePitchPreferencesRepository : VoicePitchPreferencesRepository {
    private val speed = MutableStateFlow(VOICE_PITCH_DEFAULT)

    override fun voicePitch() = speed

    override suspend fun saveVoicePitch(voicePitch: Float) {
        speed.update { voicePitch }
    }
}
