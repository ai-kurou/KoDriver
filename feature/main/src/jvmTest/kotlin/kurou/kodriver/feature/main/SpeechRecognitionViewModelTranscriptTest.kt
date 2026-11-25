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
import kurou.kodriver.domain.model.SpeechRecognitionTranscript
import kurou.kodriver.domain.repository.RecordAudioPermissionRepository
import kurou.kodriver.domain.repository.SpeechRecognitionRepository
import kurou.kodriver.domain.usecase.CheckRecordAudioPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundInCommandUseCase
import kurou.kodriver.domain.usecase.ObserveMicrophoneCutQuietSoundLevelUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechPlaybackActiveUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.PlaySpeechRecognitionChimeUseCase
import kurou.kodriver.domain.usecase.ReportSpeechRecognitionTranscriptUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kurou.kodriver.domain.usecase.SetSpeechRecognitionModeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** 聞き取った文言を、オーバーレイなど他の画面へ共有する通知のテスト。 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelTranscriptTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val transcriptRepository = FakeSpeechRecognitionTranscriptRepository()
    private val events = MutableSharedFlow<SpeechRecognitionEvent>(extraBufferCapacity = 16)
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SpeechRecognitionViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { permissionRepository.isGranted() } returns true
        every { speechRepository.observeRecognition() } returns events
        viewModel =
            SpeechRecognitionViewModel(
                observeSpeechRecognition = ObserveSpeechRecognitionUseCase(speechRepository),
                checkRecordAudioPermissionGranted = CheckRecordAudioPermissionGrantedUseCase(permissionRepository),
                requestManualReadoutItem = RequestManualReadoutItemUseCase(FakeManualReadoutItemRequestRepository()),
                setSpeechRecognitionMode = SetSpeechRecognitionModeUseCase(FakeSpeechRecognitionModeRepository()),
                observeMicrophoneCutQuietSoundLevel =
                    ObserveMicrophoneCutQuietSoundLevelUseCase(FakeMicrophoneCutQuietSoundPreferencesRepository()),
                observeMicrophoneCutQuietSoundInCommand =
                    ObserveMicrophoneCutQuietSoundInCommandUseCase(FakeMicrophoneCutQuietSoundPreferencesRepository()),
                observeSpeechPlaybackActive = ObserveSpeechPlaybackActiveUseCase(FakeSpeechPlaybackStateRepository()),
                observeSelectedSimulator = ObserveSelectedSimulatorUseCase(FakeSelectedSimulatorRepository()),
                playChime = PlaySpeechRecognitionChimeUseCase(FakeSpeechRecognitionChimePlayer()),
                reportSpeechRecognitionTranscript = ReportSpeechRecognitionTranscriptUseCase(transcriptRepository),
            )
    }

    @AfterTest
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private val transcript get() = transcriptRepository.transcript.value

    @Test
    fun `聞き取りを始める前は空の状態を通知する`() {
        assertEquals(listOf(SpeechRecognitionTranscript()), transcriptRepository.history)
    }

    @Test
    fun `認識途中の文字列を通知する`() {
        viewModel.onEnabledChange(true) {}

        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))

        assertEquals(SpeechRecognitionTranscript(partialText = "燃"), transcript)
    }

    @Test
    fun `確定した発話は先頭候補だけを通知し、認識途中の文字列は消す`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Partial("燃"))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料", "然料")))

        assertEquals(SpeechRecognitionTranscript(recognizedText = "燃料"), transcript)
    }

    @Test
    fun `確定した発話は次の発話の認識途中でも残る`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))

        events.tryEmit(SpeechRecognitionEvent.Partial("タ"))

        assertEquals(SpeechRecognitionTranscript(partialText = "タ", recognizedText = "燃料"), transcript)
    }

    @Test
    fun `次の発話が確定すると置き換える`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("タイヤ")))

        assertEquals(SpeechRecognitionTranscript(recognizedText = "タイヤ"), transcript)
    }

    @Test
    fun `聞き取りの再開通知では認識途中の文字列だけを消し、確定した発話は残す`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))
        events.tryEmit(SpeechRecognitionEvent.Partial("タ"))

        events.tryEmit(SpeechRecognitionEvent.Listening)

        assertEquals(SpeechRecognitionTranscript(recognizedText = "燃料"), transcript)
    }

    @Test
    fun `聞き取りを止めると空にする`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Partial("タ"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))

        viewModel.onEnabledChange(false) {}

        assertEquals(SpeechRecognitionTranscript(), transcript)
    }

    @Test
    fun `聞き取りに失敗すると空にする`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))

        events.tryEmit(SpeechRecognitionEvent.Failed(SpeechRecognitionFailureReason.Unavailable))

        assertEquals(SpeechRecognitionTranscript(), transcript)
    }

    @Test
    fun `聞き取りを始め直すと前回の確定した発話は引き継がない`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料")))
        viewModel.onEnabledChange(false) {}

        viewModel.onEnabledChange(true) {}

        assertEquals(SpeechRecognitionTranscript(), transcript)
    }

    @Test
    fun `同じ内容は重ねて通知しない`() {
        viewModel.onEnabledChange(true) {}
        events.tryEmit(SpeechRecognitionEvent.Level(0.3f))
        events.tryEmit(SpeechRecognitionEvent.Level(0.5f))

        assertEquals(listOf(SpeechRecognitionTranscript()), transcriptRepository.history)
    }
}
