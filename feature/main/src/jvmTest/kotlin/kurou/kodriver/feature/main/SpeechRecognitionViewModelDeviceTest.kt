package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelDeviceTest {
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

    private fun createViewModel(withDevices: Boolean = true): SpeechRecognitionViewModel =
        SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
            audioInputDeviceSelector =
                if (withDevices) audioInputDeviceSelector(deviceRepository, selectedRepository) else null,
        ).also { viewModel = it }

    @Test
    fun `入力デバイスを選べないプラットフォームでは一覧の取得も選択も何もしない`() {
        val viewModel = createViewModel(withDevices = false)

        viewModel.refreshDevices()
        viewModel.onDeviceSelected("Mic B")

        assertEquals(SpeechRecognitionUiState(), viewModel.uiState.value)
        assertEquals(0, deviceRepository.getCount)
        assertEquals(
            SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED,
            selectedRepository.saved.value,
        )
    }

    @Test
    fun `一覧を取得すると一覧と選択中のデバイスを反映し聞き取りは始めない`() {
        val viewModel = createViewModel()

        viewModel.refreshDevices()

        assertEquals(listOf("Mic A", "Mic B"), viewModel.uiState.value.devices)
        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        assertEquals(SpeechRecognitionStatus.Stopped, viewModel.uiState.value.status)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `一覧の取得に失敗しても状態は変えず聞き取りも止めない`() {
        val viewModel = createViewModel()
        deviceRepository.error = IllegalStateException("devices")
        viewModel.onEnabledChange(true) {}

        viewModel.refreshDevices()

        assertEquals(emptyList(), viewModel.uiState.value.devices)
        assertTrue(viewModel.uiState.value.enabled)
    }

    @Test
    fun `停止中にデバイスを選ぶと名前を保存して表示を変え聞き取りは始めない`() {
        val viewModel = createViewModel()
        viewModel.refreshDevices()

        viewModel.onDeviceSelected("Mic B")

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        assertEquals("Mic B", selectedRepository.saved.value)
        verify(exactly = 0) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `表示中の先頭のデバイスを選ぶと保存だけ行い聞き取りはやり直さない`() {
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        viewModel.onEnabledChange(true) {}

        viewModel.onDeviceSelected("Mic A")

        assertEquals("Mic A", selectedRepository.saved.value)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `聞き取り中にデバイスを選ぶと保存してから選んだデバイスで聞き取りをやり直す`() {
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        val savedAtRestart = mutableListOf<String>()
        every { speechRepository.observeRecognition() } answers {
            savedAtRestart += selectedRepository.saved.value
            events
        }
        viewModel.onEnabledChange(true) {}

        viewModel.onDeviceSelected("Mic B")

        assertEquals(
            listOf(SELECTED_AUDIO_INPUT_DEVICE_NAME_UNSPECIFIED, "Mic B"),
            savedAtRestart,
        )
        assertTrue(viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.Starting, viewModel.uiState.value.status)
    }

    @Test
    fun `保存に失敗したら表示を元に戻し聞き取りはやり直さない`() {
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        viewModel.onEnabledChange(true) {}
        selectedRepository.saveError = IllegalStateException("save")

        viewModel.onDeviceSelected("Mic B")

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `聞き取りを始めるときに一覧を取り直して表示を実際に開くデバイスに揃える`() {
        selectedRepository.saved.value = "Mic B"
        deviceRepository.devices = listOf(AudioInputDevice("Mic A"))
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        deviceRepository.devices = listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B"))

        viewModel.onEnabledChange(true) {}

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
    }

    @Test
    fun `停止した聞き取りがマイクを閉じ終えるまで次の聞き取りを開始しない`() {
        val released = CompletableDeferred<Unit>()
        every { speechRepository.observeRecognition() } returns
            flow {
                try {
                    awaitCancellation()
                } finally {
                    withContext(NonCancellable) { released.await() }
                }
            }
        val viewModel = createViewModel()
        viewModel.onEnabledChange(true) {}

        viewModel.stop()
        viewModel.onEnabledChange(true) {}
        verify(exactly = 1) { speechRepository.observeRecognition() }

        released.complete(Unit)
        verify(exactly = 2) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `聞き取り中に選択中のデバイスが変わらなければやり直さない`() {
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        viewModel.onEnabledChange(true) {}

        viewModel.refreshDevices()

        verify(exactly = 1) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }

    @Test
    fun `聞き取り中に選択中のデバイスが一覧から消えたら先頭に切り替えて聞き取りをやり直す`() {
        selectedRepository.saved.value = "Mic B"
        val viewModel = createViewModel()
        viewModel.refreshDevices()
        viewModel.onEnabledChange(true) {}
        deviceRepository.devices = listOf(AudioInputDevice("Mic A"))

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        verify(exactly = 2) { speechRepository.observeRecognition() }
        confirmVerified(speechRepository)
    }
}
