package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MicrophoneInputViewModelSelectionTest {
    private val microphoneRepository: MicrophoneRepository = mockk()
    private val deviceRepository: AudioInputDeviceRepository = mockk()
    private val events = MutableSharedFlow<MicrophoneAudioEvent>(extraBufferCapacity = 16)
    private val selectedRepository = FakeSelectedAudioInputDevicePreferencesRepository()
    private lateinit var viewModel: MicrophoneInputViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            microphoneInputViewModel(microphoneRepository, deviceRepository, selectedRepository)
    }

    @AfterTest
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private val micA = AudioInputDevice("Mic A")

    private fun stubDevices(devices: List<AudioInputDevice> = listOf(micA, AudioInputDevice("Mic B"))) {
        coEvery { deviceRepository.getDevices() } returns devices
        every { microphoneRepository.observeAudio(devices.firstOrNull()) } returns events
    }

    @Test
    fun `デバイスを選ぶと名前を保存する`() {
        stubDevices()
        viewModel.refreshDevices()

        viewModel.onDeviceSelected("Mic B")

        assertEquals("Mic B", selectedRepository.saved.value)
    }

    @Test
    fun `保存済みのデバイスが一覧にあれば先頭ではなくそれを選ぶ`() {
        selectedRepository.saved.value = "Mic B"
        stubDevices()

        viewModel.refreshDevices()

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
    }

    @Test
    fun `保存済みのデバイスが一覧になければ先頭を使い、保存値は書き換えない`() {
        selectedRepository.saved.value = "Mic X"
        stubDevices()

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        assertEquals("Mic X", selectedRepository.saved.value)
    }

    @Test
    fun `保存済みのデバイスが後から現れても、一覧に先頭が出ている間は切り替えない`() {
        selectedRepository.saved.value = "Mic X"
        stubDevices()
        viewModel.refreshDevices()
        coEvery { deviceRepository.getDevices() } returns listOf(micA, AudioInputDevice("Mic X"))

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
    }

    @Test
    fun `デバイスが0件の間は保存済みの名前を保持し、現れたらそれを選ぶ`() {
        selectedRepository.saved.value = "Mic B"
        coEvery { deviceRepository.getDevices() } returns emptyList()
        viewModel.refreshDevices()
        assertEquals(null, viewModel.uiState.value.deviceName)
        stubDevices()

        viewModel.refreshDevices()

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
    }

    @Test
    fun `保存済みのデバイスでマイクの取得を開始する`() {
        selectedRepository.saved.value = "Mic B"
        stubDevices()
        val micB = AudioInputDevice("Mic B")
        every { microphoneRepository.observeAudio(micB) } returns events

        viewModel.onEnabledChange(true)

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        verify(exactly = 1) { microphoneRepository.observeAudio(micB) }
        coVerify(exactly = 1) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `保存済みの名前の読み込み前にユーザーが選んだ場合は、ユーザーの選択を優先する`() {
        selectedRepository.saved.value = "Mic A"
        stubDevices()

        viewModel.onDeviceSelected("Mic B")
        viewModel.refreshDevices()

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
    }

    @Test
    fun `名前の保存に失敗しても選択は有効なまま`() {
        val failing =
            object : SelectedAudioInputDevicePreferencesRepository {
                override fun selectedDeviceName() = flowOf("")

                override suspend fun saveSelectedDeviceName(name: String) = throw IllegalStateException("boom")
            }
        stubDevices()
        val vm = microphoneInputViewModel(microphoneRepository, deviceRepository, failing)
        try {
            vm.refreshDevices()

            vm.onDeviceSelected("Mic B")

            assertEquals("Mic B", vm.uiState.value.deviceName)
        } finally {
            vm.viewModelScope.cancel()
        }
    }

    @Test
    fun `保存済みの名前の読み込みに失敗しても状態は変えない`() {
        val failing =
            object : SelectedAudioInputDevicePreferencesRepository {
                override fun selectedDeviceName() = flow<String> { throw IllegalStateException("boom") }

                override suspend fun saveSelectedDeviceName(name: String) = Unit
            }
        stubDevices()
        val vm = microphoneInputViewModel(microphoneRepository, deviceRepository, failing)
        try {
            vm.refreshDevices()

            assertEquals(MicrophoneInputUiState(), vm.uiState.value)
        } finally {
            vm.viewModelScope.cancel()
        }
    }
}
