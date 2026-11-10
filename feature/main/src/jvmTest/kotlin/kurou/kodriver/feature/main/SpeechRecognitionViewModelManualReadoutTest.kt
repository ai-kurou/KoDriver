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
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechRecognitionEvent
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
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

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelManualReadoutTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
    private val simulatorRepository = FakeSelectedSimulatorRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private var viewModel: SpeechRecognitionViewModel? = null

    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
        registerGt7Ps5ReadoutItemKeys()
        registerAceWindowsReadoutItemKeys()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        viewModel?.viewModelScope?.cancel()
        Dispatchers.resetMain()
    }

    private fun createListeningViewModel(): SpeechRecognitionViewModel {
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        return SpeechRecognitionViewModel(
            observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
            checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
            requestManualReadoutItem = RequestManualReadoutItemUseCase(itemRequestRepository),
            setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
            observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
            playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
        ).also {
            viewModel = it
            it.onEnabledChange(true) {}
            // 語句はウェイクワードの後のコマンド待ちの間だけ受け付ける
            events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        }
    }

    @Test
    fun `確定結果が手動読み上げ項目の語句に一致すると選択中のシミュレーターの項目依頼だけを発行する`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))
        // 依頼を出すとウェイクワード待ちへ戻るため、次の語句にはもう一度ウェイクワードが要る
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("関係ない言葉", "自己ベスト")))

        assertEquals(
            listOf(
                voiceRequest(Simulator.LmuWindows.id, LmuWindowsReadoutItemKey.RemainingFuel.Laps.value),
                voiceRequest(Simulator.LmuWindows.id, LmuWindowsReadoutItemKey.MyBestLap.Root.value),
            ),
            itemRequestRepository.requests,
        )
    }

    @Test
    fun `GT7を選択中ならGT7の項目だけを依頼する`() {
        simulatorRepository.selected.value = Simulator.Gt7Ps5
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(
            listOf(voiceRequest(Simulator.Gt7Ps5.id, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value)),
            itemRequestRepository.requests,
        )
    }

    @Test
    fun `ACEを選択中ならACEの項目だけを依頼する`() {
        simulatorRepository.selected.value = Simulator.AceWindows
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(
            listOf(voiceRequest(Simulator.AceWindows.id, AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value)),
            itemRequestRepository.requests,
        )
    }

    @Test
    fun `選択中でないシミュレーターの語句だけに一致したときは依頼せずコマンド待ちを続ける`() {
        simulatorRepository.selected.value = Simulator.Gt7Ps5
        val viewModel = createListeningViewModel()

        // 「ブレーキ温度」はLMUの語句で、GT7には無い
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("ブレーキ温度を教えて")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(SpeechRecognitionStatus.CommandWaiting, viewModel.uiState.value.status)
    }

    @Test
    fun `語句に一致しない確定結果や認識途中の文字列では依頼を発行しない`() {
        val viewModel = createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("燃料"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(listOf(listOf("こんにちは"), listOf("コドライバー")), viewModel.uiState.value.recognized)
    }

    @Test
    fun `音声認識由来の項目依頼は開始音を鳴らさない指定で発行する`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(
            listOf(false),
            itemRequestRepository.requests.map { it.playStartSound },
        )
    }

    @Test
    fun `項目依頼に失敗しても聞き取りを止めずウェイクワード待ちへ戻る`() {
        val viewModel = createListeningViewModel()
        itemRequestRepository.failure = IllegalStateException("失敗")

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(true, viewModel.uiState.value.enabled)
        assertEquals(SpeechRecognitionStatus.Listening, viewModel.uiState.value.status)

        // 次のウェイクワードとコマンドは、失敗の後でも受け付ける
        itemRequestRepository.failure = null
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("コドライバー")))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))

        assertEquals(
            listOf(voiceRequest(Simulator.LmuWindows.id, LmuWindowsReadoutItemKey.RemainingFuel.Laps.value)),
            itemRequestRepository.requests,
        )
    }

    private fun voiceRequest(
        simulatorId: String,
        itemKey: String,
    ) = ManualReadoutItemRequest(simulatorId, itemKey, playStartSound = false)
}
