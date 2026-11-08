package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.SpeechRecognitionFailureReason
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelFailureReasonTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun assertFailureStatus(
        reason: SpeechRecognitionFailureReason,
        expected: SpeechRecognitionStatus,
    ) {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        val viewModel =
            SpeechRecognitionViewModel(
                observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
                checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
                requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
                setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
                observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
                playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
            ).also { this.viewModel = it }
        viewModel.onEnabledChange(true) {}

        events.tryEmit(SpeechRecognitionEvent.Failed(reason))

        assertFalse(viewModel.uiState.value.enabled)
        assertEquals(expected, viewModel.uiState.value.status)
    }

    @Test
    fun `マイクを開けない失敗はマイク利用不可として表示する`() {
        assertFailureStatus(
            SpeechRecognitionFailureReason.MicrophoneUnavailable,
            SpeechRecognitionStatus.MicrophoneUnavailable,
        )
    }

    @Test
    fun `無音が続いた失敗は音声が届いていないものとして表示する`() {
        assertFailureStatus(SpeechRecognitionFailureReason.NoSignal, SpeechRecognitionStatus.NoSignal)
    }

    @Test
    fun `認識中のエラーは認識失敗として表示する`() {
        assertFailureStatus(SpeechRecognitionFailureReason.RecognitionFailed, SpeechRecognitionStatus.RecognitionFailed)
    }
}
