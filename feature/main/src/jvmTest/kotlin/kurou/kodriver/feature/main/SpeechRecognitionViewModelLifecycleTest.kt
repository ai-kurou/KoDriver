package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelLifecycleTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private val dispatcher = UnconfinedTestDispatcher()
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun createViewModel(granted: Boolean): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns granted
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
            playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
        ).also { viewModel = it }
    }

    @Test
    fun `見えなくなると止めて前面に戻ると再開する`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}

        viewModel.onAppBackgrounded()

        assertFalse(viewModel.uiState.value.enabled)

        viewModel.onAppForegrounded()

        assertEquals(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Starting),
            viewModel.uiState.value,
        )
        verify(exactly = 2) { permissionRepository.isGranted() }
        verify(exactly = 2) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `停止中に見えなくなっても前面に戻って再開しない`() {
        val viewModel = createViewModel(granted = true)

        viewModel.onAppBackgrounded()
        viewModel.onAppForegrounded()

        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `スイッチでOFFにした後は前面に戻っても再開しない`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        viewModel.onAppBackgrounded()

        viewModel.onEnabledChange(false) {}
        viewModel.onAppForegrounded()

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 1) { permissionRepository.isGranted() }
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `前面に戻ったときに権限が外されていれば再開しない`() {
        val viewModel = createViewModel(granted = true)
        viewModel.onEnabledChange(true) {}
        viewModel.onAppBackgrounded()
        every { permissionRepository.isGranted() } returns false

        viewModel.onAppForegrounded()

        assertFalse(viewModel.uiState.value.enabled)
        verify(exactly = 2) { permissionRepository.isGranted() }
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `起動時に権限があれば聞き取りを始め二度目の呼び出しは無視する`() {
        val viewModel = createViewModel(granted = true)

        viewModel.onAppLaunched()
        viewModel.onAppLaunched()

        assertEquals(
            SpeechRecognitionUiState(enabled = true, status = SpeechRecognitionStatus.Starting),
            viewModel.uiState.value,
        )
        verify(exactly = 1) { permissionRepository.isGranted() }
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(permissionRepository, speechRepository)
    }

    @Test
    fun `起動時に権限がなければ何もしない`() {
        val viewModel = createViewModel(granted = false)

        viewModel.onAppLaunched()

        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        verify(exactly = 1) { permissionRepository.isGranted() }
        confirmVerified(permissionRepository, speechRepository)
    }
}
