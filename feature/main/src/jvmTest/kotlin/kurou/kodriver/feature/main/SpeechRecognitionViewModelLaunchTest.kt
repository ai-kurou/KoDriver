package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.SpeechRecognitionEvent
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
import kotlin.test.assertTrue

/** デスクトップで、起動時に保存済みの入力デバイスがあれば聞き取りを自動で始める動作（[SpeechRecognitionViewModel.onAppLaunched]）のテスト。 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelLaunchTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private val deviceRepository =
        FakeAudioInputDeviceRepository(listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B")))
    private val selectedRepository = FakeSelectedAudioInputDevicePreferencesRepository()
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SpeechRecognitionViewModel =
        SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
            playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
            audioInputDeviceSelector = audioInputDeviceSelector(deviceRepository, selectedRepository),
        ).also { viewModel = it }

    @Test
    fun `起動時に保存済みのデバイスがあれば始める`() {
        selectedRepository.saved.value = "Mic B"
        val viewModel = createViewModel()

        viewModel.onAppLaunched()

        assertTrue(viewModel.uiState.value.enabled)
        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時に保存名が一覧になければ先頭のデバイスで始める`() {
        selectedRepository.saved.value = "Mic X"
        val viewModel = createViewModel()

        viewModel.onAppLaunched()

        assertTrue(viewModel.uiState.value.enabled)
        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時にデバイスが未選択なら始めない`() {
        val viewModel = createViewModel()

        viewModel.onAppLaunched()

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時に保存済みでもデバイスが0件なら始めない`() {
        selectedRepository.saved.value = "Mic B"
        deviceRepository.devices = emptyList()
        val viewModel = createViewModel()

        viewModel.onAppLaunched()

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時に一覧の取得に失敗したら始めない`() {
        selectedRepository.saved.value = "Mic B"
        deviceRepository.error = IllegalStateException("devices")
        val viewModel = createViewModel()

        viewModel.onAppLaunched()

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時の一覧の取得中にOFFにしても取得が終わってから始めない`() {
        selectedRepository.saved.value = "Mic B"
        val gate = CompletableDeferred<Unit>().also { deviceRepository.gate = it }
        val viewModel = createViewModel()
        viewModel.onAppLaunched()

        viewModel.onEnabledChange(false) {}
        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時の一覧の取得中に見えなくなっても取得が終わってから始めない`() {
        selectedRepository.saved.value = "Mic B"
        val gate = CompletableDeferred<Unit>().also { deviceRepository.gate = it }
        val viewModel = createViewModel()
        viewModel.onAppLaunched()

        viewModel.onAppBackgrounded()
        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時の一覧の取得中にデバイスを選んでも停止中のままで始めない`() {
        val gate = CompletableDeferred<Unit>().also { deviceRepository.gate = it }
        val viewModel = createViewModel()
        viewModel.onAppLaunched()

        viewModel.onDeviceSelected("Mic B")
        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `起動時の一覧の取得中にONにした場合は一度だけ始める`() {
        selectedRepository.saved.value = "Mic B"
        val gate = CompletableDeferred<Unit>().also { deviceRepository.gate = it }
        val viewModel = createViewModel()
        viewModel.onAppLaunched()

        viewModel.onEnabledChange(true) {}
        gate.complete(Unit)

        assertTrue(viewModel.uiState.value.enabled)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }
}
