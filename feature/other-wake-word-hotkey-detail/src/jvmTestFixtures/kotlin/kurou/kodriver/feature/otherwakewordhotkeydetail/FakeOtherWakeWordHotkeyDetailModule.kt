package kurou.kodriver.feature.otherwakewordhotkeydetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** ウェイクワードホットキー詳細が消費する Repository の Fake をバインドするテスト用モジュール（testFixtures）。 */
val fakeOtherWakeWordHotkeyDetailModule =
    module {
        single<WakeWordHotkeyPreferencesRepository> { FakeWakeWordHotkeyPreferencesRepository() }
        single<WakeWordHotkeyRepository> { FakeWakeWordHotkeyRepository() }
        single<SpeechRecognitionEnabledRepository> { FakeDetailSpeechRecognitionEnabledRepository() }
        Simulator.entries.forEach { simulator ->
            single<ManualReadoutSetPreferencesRepository>(named(simulator.id)) {
                FakeEmptyManualReadoutSetPreferencesRepository()
            }
        }
    }

class FakeDetailSpeechRecognitionEnabledRepository : SpeechRecognitionEnabledRepository {
    private val state = MutableStateFlow(false)
    override val enabled: StateFlow<Boolean> = state

    override fun update(enabled: Boolean) {
        state.value = enabled
    }
}

class FakeWakeWordHotkeyPreferencesRepository : WakeWordHotkeyPreferencesRepository {
    private val hotkey = MutableStateFlow(WakeWordHotkey())

    override fun observeWakeWordHotkey(): Flow<WakeWordHotkey> = hotkey

    override suspend fun saveWakeWordHotkey(hotkey: WakeWordHotkey) {
        this.hotkey.value = hotkey
    }
}

class FakeWakeWordHotkeyRepository : WakeWordHotkeyRepository {
    override fun pressStream(): Flow<Unit> = emptyFlow()

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration() = Unit
}

class FakeEmptyManualReadoutSetPreferencesRepository : ManualReadoutSetPreferencesRepository {
    override fun observeSets(): Flow<List<ManualReadoutSet>> = flowOf(emptyList())

    override suspend fun addSet(set: ManualReadoutSet) = Unit

    override suspend fun updateSet(set: ManualReadoutSet) = Unit

    override suspend fun deleteSet(id: String) = Unit

    override suspend fun saveSets(sets: List<ManualReadoutSet>) = Unit

    override suspend fun resetToDefault() = Unit
}
