package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.speechrecognition.repository.SpeechRecognitionEnabledRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import kurou.kodriver.domain.speechrecognition.usecase.ObserveWakeWordHotkeyPressesUseCase
import kurou.kodriver.domain.speechrecognition.usecase.ReportSpeechRecognitionEnabledUseCase

class FakeWakeWordHotkeyRepository : WakeWordHotkeyRepository {
    val presses = MutableSharedFlow<Unit>(extraBufferCapacity = 16)

    override fun pressStream(): Flow<Unit> = presses

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> =
        MutableStateFlow(HotkeyRegistrationState.Unsupported)

    override suspend fun retryRegistration() = Unit
}

class FakeSpeechRecognitionEnabledRepository : SpeechRecognitionEnabledRepository {
    private val state = MutableStateFlow(false)
    override val enabled: StateFlow<Boolean> = state

    override fun update(enabled: Boolean) {
        state.value = enabled
    }
}

fun fakeObserveWakeWordHotkeyPresses() = ObserveWakeWordHotkeyPressesUseCase(FakeWakeWordHotkeyRepository())

fun fakeReportSpeechRecognitionEnabled() =
    ReportSpeechRecognitionEnabledUseCase(FakeSpeechRecognitionEnabledRepository())
