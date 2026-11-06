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
import kurou.kodriver.domain.usecase.ObserveSpeechRecognitionUseCase
import kurou.kodriver.domain.usecase.RequestManualReadoutItemUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionViewModelManualReadoutTest {
    private val speechRepository: SpeechRecognitionRepository = mockk()
    private val permissionRepository: RecordAudioPermissionRepository = mockk()
    private val itemRequestRepository = FakeManualReadoutItemRequestRepository()
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
        ).also {
            viewModel = it
            it.onEnabledChange(true) {}
        }
    }

    @Test
    fun `確定結果が手動読み上げ項目の語句に一致するとLMUとGT7とACEの項目依頼を発行する`() {
        createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("燃料周回を教えて")))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("関係ない言葉", "じこべ")))

        assertEquals(
            listOf(
                ManualReadoutItemRequest(Simulator.LmuWindows.id, LmuWindowsReadoutItemKey.RemainingFuel.Laps.value),
                ManualReadoutItemRequest(Simulator.Gt7Ps5.id, Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value),
                ManualReadoutItemRequest(
                    Simulator.AceWindows.id,
                    AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value,
                ),
                ManualReadoutItemRequest(Simulator.LmuWindows.id, LmuWindowsReadoutItemKey.MyBestLap.Root.value),
                ManualReadoutItemRequest(Simulator.Gt7Ps5.id, Gt7Ps5ReadoutItemKey.MyBestLap.Root.value),
                ManualReadoutItemRequest(Simulator.AceWindows.id, AceWindowsReadoutItemKey.MyBestLap.Root.value),
            ),
            itemRequestRepository.requests,
        )
    }

    @Test
    fun `語句に一致しない確定結果や認識途中の文字列では依頼を発行しない`() {
        val viewModel = createListeningViewModel()

        events.tryEmit(SpeechRecognitionEvent.Partial("燃料"))
        events.tryEmit(SpeechRecognitionEvent.Recognized(listOf("こんにちは")))

        assertEquals(emptyList(), itemRequestRepository.requests)
        assertEquals(listOf(listOf("こんにちは")), viewModel.uiState.value.recognized)
    }
}
