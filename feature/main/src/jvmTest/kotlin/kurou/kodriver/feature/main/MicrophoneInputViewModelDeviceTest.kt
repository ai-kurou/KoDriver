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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MicrophoneInputViewModelDeviceTest {
    private val microphoneRepository: MicrophoneRepository = mockk()
    private val deviceRepository: AudioInputDeviceRepository = mockk()
    private val events = MutableSharedFlow<MicrophoneAudioEvent>(extraBufferCapacity = 16)
    private lateinit var viewModel: MicrophoneInputViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            microphoneInputViewModel(microphoneRepository, deviceRepository)
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
    fun `一覧を取得すると停止中でも先頭のデバイス名と一覧を表示する`() {
        stubDevices()

        viewModel.refreshDevices()

        assertEquals(
            MicrophoneInputUiState(deviceName = "Mic A", devices = listOf("Mic A", "Mic B")),
            viewModel.uiState.value,
        )
        coVerify(exactly = 1) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `一覧の取得で例外が起きても状態は変えない`() {
        coEvery { deviceRepository.getDevices() } throws IllegalStateException("boom")

        viewModel.refreshDevices()

        assertEquals(MicrophoneInputUiState(), viewModel.uiState.value)
    }

    @Test
    fun `停止中にデバイスを選ぶと表示を切り替え、ONにしたとき選んだデバイスで取得する`() {
        stubDevices()
        val micB = AudioInputDevice("Mic B")
        every { microphoneRepository.observeAudio(micB) } returns events
        viewModel.refreshDevices()

        viewModel.onDeviceSelected("Mic B")

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        assertEquals(false, viewModel.uiState.value.enabled)

        viewModel.onEnabledChange(true)

        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        verify(exactly = 1) { microphoneRepository.observeAudio(micB) }
        coVerify(exactly = 2) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `取得中にデバイスを選ぶと選んだデバイスで取得し直す`() {
        stubDevices()
        val micB = AudioInputDevice("Mic B")
        every { microphoneRepository.observeAudio(micB) } returns events
        viewModel.onEnabledChange(true)

        viewModel.onDeviceSelected("Mic B")

        assertEquals(true, viewModel.uiState.value.enabled)
        assertEquals("Mic B", viewModel.uiState.value.deviceName)
        assertEquals(1, events.subscriptionCount.value)
        verify(exactly = 1) { microphoneRepository.observeAudio(micA) }
        verify(exactly = 1) { microphoneRepository.observeAudio(micB) }
        coVerify(exactly = 2) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `選択中のデバイスが選択済みと同じなら何もしない`() {
        stubDevices()
        viewModel.onEnabledChange(true)
        viewModel.onDeviceSelected("Mic A")
        viewModel.onDeviceSelected("Mic A")

        coVerify(exactly = 1) { deviceRepository.getDevices() }
        verify(exactly = 1) { microphoneRepository.observeAudio(micA) }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `選んだデバイスが一覧から消えたときは先頭のデバイスに戻す`() {
        stubDevices()
        viewModel.refreshDevices()
        viewModel.onDeviceSelected("Mic B")
        coEvery { deviceRepository.getDevices() } returns listOf(micA)

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        assertEquals(listOf("Mic A"), viewModel.uiState.value.devices)
    }

    @Test
    fun `取得中に一覧の更新で解決先のデバイスが変わったら取得し直す`() {
        stubDevices()
        val micB = AudioInputDevice("Mic B")
        every { microphoneRepository.observeAudio(micB) } returns events
        viewModel.onEnabledChange(true)
        viewModel.onDeviceSelected("Mic B")
        coEvery { deviceRepository.getDevices() } returns listOf(micA)

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        assertEquals(1, events.subscriptionCount.value)
        verify(exactly = 2) { microphoneRepository.observeAudio(micA) }
        verify(exactly = 1) { microphoneRepository.observeAudio(micB) }
        coVerify(exactly = 4) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `一覧から消えた選択は破棄され、再接続されても元のデバイスに戻らない`() {
        stubDevices()
        viewModel.refreshDevices()
        viewModel.onDeviceSelected("Mic B")
        coEvery { deviceRepository.getDevices() } returns listOf(micA)
        viewModel.refreshDevices()
        coEvery { deviceRepository.getDevices() } returns listOf(micA, AudioInputDevice("Mic B"))

        viewModel.refreshDevices()

        assertEquals("Mic A", viewModel.uiState.value.deviceName)
    }
}
