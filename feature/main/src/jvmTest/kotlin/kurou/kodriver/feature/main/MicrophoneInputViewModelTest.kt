package kurou.kodriver.feature.main

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
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
import kurou.kodriver.domain.model.MicrophoneAudioEvent
import kurou.kodriver.domain.model.MicrophoneFailureReason
import kurou.kodriver.domain.repository.AudioInputDeviceRepository
import kurou.kodriver.domain.repository.MicrophoneRepository
import kurou.kodriver.domain.usecase.GetAudioInputDevicesUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneInputStatusUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MicrophoneInputViewModelTest {
    private val microphoneRepository: MicrophoneRepository = mockk()
    private val deviceRepository: AudioInputDeviceRepository = mockk()
    private val events = MutableSharedFlow<MicrophoneAudioEvent>(extraBufferCapacity = 16)
    private lateinit var viewModel: MicrophoneInputViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel =
            MicrophoneInputViewModel(
                observeMicrophoneInputStatus = ObserveMicrophoneInputStatusUseCase(microphoneRepository),
                getAudioInputDevices = GetAudioInputDevicesUseCase(deviceRepository),
            )
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

    private fun loudChunk() = MicrophoneAudioEvent.Chunk(byteArrayOf(0, 0x40, 0, 0x40))

    @Test
    fun `初期状態は停止中`() {
        assertEquals(MicrophoneInputUiState(), viewModel.uiState.value)
    }

    @Test
    fun `ONにすると先頭のデバイス名を表示し、そのデバイスで取得して音量を表示する`() {
        stubDevices()
        viewModel.onEnabledChange(true)

        assertEquals(
            MicrophoneInputUiState(enabled = true, status = MicrophoneInputStatus.Starting, deviceName = "Mic A"),
            viewModel.uiState.value,
        )

        events.tryEmit(loudChunk())

        assertEquals(MicrophoneInputStatus.Capturing, viewModel.uiState.value.status)
        assertEquals(0.5f, viewModel.uiState.value.level)
        coVerify(exactly = 1) { deviceRepository.getDevices() }
        verify(exactly = 1) { microphoneRepository.observeAudio(micA) }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `デバイスが無いときはデバイス名なしで取得を続ける`() {
        stubDevices(emptyList())

        viewModel.onEnabledChange(true)

        assertEquals(null, viewModel.uiState.value.deviceName)
        assertEquals(MicrophoneInputStatus.Starting, viewModel.uiState.value.status)
        coVerify(exactly = 1) { deviceRepository.getDevices() }
        verify(exactly = 1) { microphoneRepository.observeAudio(null) }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `OFFにすると停止して音量を0に戻す`() {
        stubDevices()
        viewModel.onEnabledChange(true)
        events.tryEmit(loudChunk())

        viewModel.onEnabledChange(false)

        assertEquals(false, viewModel.uiState.value.enabled)
        assertEquals(MicrophoneInputStatus.Stopped, viewModel.uiState.value.status)
        assertEquals(0f, viewModel.uiState.value.level)
        assertEquals(0, events.subscriptionCount.value)
    }

    @Test
    fun `再度ONにするとデバイス一覧の取得を待つ間は前のデバイス名を消す`() {
        stubDevices()
        viewModel.onEnabledChange(true)
        assertEquals("Mic A", viewModel.uiState.value.deviceName)
        val pending = CompletableDeferred<List<AudioInputDevice>>()
        coEvery { deviceRepository.getDevices() } coAnswers { pending.await() }

        viewModel.onEnabledChange(true)

        assertEquals(null, viewModel.uiState.value.deviceName)
        assertEquals(MicrophoneInputStatus.Starting, viewModel.uiState.value.status)
    }

    @Test
    fun `再度ONにすると前の取得がマイクを閉じ終えるまで次の取得を始めない`() {
        stubDevices()
        val closing = CompletableDeferred<Unit>()
        every { microphoneRepository.observeAudio(micA) } returnsMany
            listOf(
                flow<MicrophoneAudioEvent> {
                    try {
                        awaitCancellation()
                    } finally {
                        withContext(NonCancellable) { closing.await() }
                    }
                },
                events,
            )
        viewModel.onEnabledChange(true)

        viewModel.onEnabledChange(true)

        verify(exactly = 1) { microphoneRepository.observeAudio(micA) }
        assertEquals(0, events.subscriptionCount.value)

        closing.complete(Unit)

        verify(exactly = 2) { microphoneRepository.observeAudio(micA) }
        assertEquals(1, events.subscriptionCount.value)
        coVerify(exactly = 2) { deviceRepository.getDevices() }
        confirmVerified(deviceRepository, microphoneRepository)
    }

    @Test
    fun `停止中にOFFを操作しても状態は変わらない`() {
        viewModel.onEnabledChange(false)

        assertEquals(MicrophoneInputUiState(), viewModel.uiState.value)
    }

    @Test
    fun `無音が続いたときはOFFにして案内用の状態にする`() {
        stubDevices()
        viewModel.onEnabledChange(true)

        events.tryEmit(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.NoSignal))

        assertEquals(false, viewModel.uiState.value.enabled)
        assertEquals(MicrophoneInputStatus.NoSignal, viewModel.uiState.value.status)
    }

    @Test
    fun `デバイスを開けないときは利用不可にする`() {
        stubDevices()
        viewModel.onEnabledChange(true)

        events.tryEmit(MicrophoneAudioEvent.Failed(MicrophoneFailureReason.Unavailable))

        assertEquals(false, viewModel.uiState.value.enabled)
        assertEquals(MicrophoneInputStatus.Unavailable, viewModel.uiState.value.status)
    }

    @Test
    fun `デバイス一覧の取得で例外が起きたときは利用不可にする`() {
        coEvery { deviceRepository.getDevices() } throws IllegalStateException("boom")

        viewModel.onEnabledChange(true)

        assertEquals(false, viewModel.uiState.value.enabled)
        assertEquals(MicrophoneInputStatus.Unavailable, viewModel.uiState.value.status)
    }

    @Test
    fun `取得中にバックグラウンドへ移ると止め前面に戻ると再開する`() {
        stubDevices()
        viewModel.onEnabledChange(true)

        viewModel.onAppBackgrounded()

        assertEquals(MicrophoneInputStatus.Stopped, viewModel.uiState.value.status)
        assertEquals(0, events.subscriptionCount.value)

        viewModel.onAppForegrounded()

        assertEquals(true, viewModel.uiState.value.enabled)
        assertEquals(MicrophoneInputStatus.Starting, viewModel.uiState.value.status)
        assertEquals(1, events.subscriptionCount.value)
    }

    @Test
    fun `停止中にバックグラウンドへ移っても前面に戻って再開しない`() {
        viewModel.onAppBackgrounded()
        viewModel.onAppForegrounded()

        assertEquals(MicrophoneInputUiState(), viewModel.uiState.value)
        assertEquals(0, events.subscriptionCount.value)
    }

    @Test
    fun `ONを重ねて操作しても取得は1つだけ`() {
        stubDevices()
        viewModel.onEnabledChange(true)
        viewModel.onEnabledChange(true)

        assertEquals(1, events.subscriptionCount.value)
    }
}
