package kurou.kodriver.feature.speechrecognitionlist

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ManualReadoutItemRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository
import kurou.kodriver.domain.usecase.ObserveManualReadoutItemSpokenUseCase
import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val guideRepository: SpeechCommandGuideRepository = mockk()
    private val spokenRepository: ManualReadoutItemSpokenRepository = mockk()
    private val spoken = MutableSharedFlow<ManualReadoutItemRequest>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `選択中のシミュレーターの案内を表示状態にする`() =
        runTest(dispatcher) {
            val groups =
                listOf(
                    SpeechCommandGuideGroup(
                        ManualReadoutItemCategory.FUEL,
                        listOf(SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料", "ガス"))),
                    ),
                )
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.LmuWindows)
            every { guideRepository.guideGroups(Simulator.LmuWindows) } returns groups
            every { spokenRepository.observeSpoken() } returns spoken
            val viewModel = createViewModel()
            try {
                assertEquals(
                    SpeechRecognitionListUiState(groups),
                    viewModel.uiState.first { it.groups.isNotEmpty() },
                )
                verify(exactly = 1) { simulatorRepository.selectedSimulator() }
                verify(exactly = 1) { guideRepository.guideGroups(Simulator.LmuWindows) }
                verify(exactly = 1) { spokenRepository.observeSpoken() }
                confirmVerified(simulatorRepository, guideRepository, spokenRepository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `読み上げられた語句を強調する指示にし同じ語句を続けても指示を更新する`() =
        runTest(dispatcher) {
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.LmuWindows)
            every { guideRepository.guideGroups(Simulator.LmuWindows) } returns emptyList()
            every { spokenRepository.observeSpoken() } returns spoken
            val viewModel = createViewModel()
            val collector = launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                assertNull(viewModel.uiState.value.highlight)
                spoken.emit(ManualReadoutItemRequest(Simulator.LmuWindows.id, "fuel", matchedPhrase = "ガス"))
                assertEquals(SpeechPhraseHighlight("fuel", "ガス", 1), viewModel.uiState.value.highlight)
                spoken.emit(ManualReadoutItemRequest(Simulator.LmuWindows.id, "fuel", matchedPhrase = "ガス"))
                assertEquals(SpeechPhraseHighlight("fuel", "ガス", 2), viewModel.uiState.value.highlight)
            } finally {
                collector.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `強調の指示は点滅が終わる頃に取り下げ次の読み上げでまた出す`() =
        runTest(dispatcher) {
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.LmuWindows)
            every { guideRepository.guideGroups(Simulator.LmuWindows) } returns emptyList()
            every { spokenRepository.observeSpoken() } returns spoken
            val viewModel = createViewModel()
            val collector = launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                spoken.emit(ManualReadoutItemRequest(Simulator.LmuWindows.id, "fuel", matchedPhrase = "ガス"))
                dispatcher.scheduler.advanceTimeBy(HIGHLIGHT_DURATION_MS - 1)
                assertEquals(SpeechPhraseHighlight("fuel", "ガス", 1), viewModel.uiState.value.highlight)
                dispatcher.scheduler.advanceTimeBy(1)
                dispatcher.scheduler.runCurrent()
                assertNull(viewModel.uiState.value.highlight)
                spoken.emit(ManualReadoutItemRequest(Simulator.LmuWindows.id, "fuel", matchedPhrase = "ガス"))
                assertEquals(SpeechPhraseHighlight("fuel", "ガス", 2), viewModel.uiState.value.highlight)
            } finally {
                collector.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `一致した語句のない依頼は強調しない`() =
        runTest(dispatcher) {
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.LmuWindows)
            every { guideRepository.guideGroups(Simulator.LmuWindows) } returns emptyList()
            every { spokenRepository.observeSpoken() } returns spoken
            val viewModel = createViewModel()
            val collector = launch(dispatcher) { viewModel.uiState.collect {} }
            try {
                spoken.emit(ManualReadoutItemRequest(Simulator.LmuWindows.id, "fuel"))
                assertNull(viewModel.uiState.value.highlight)
            } finally {
                collector.cancel()
                viewModel.viewModelScope.cancel()
            }
        }

    private fun createViewModel() =
        SpeechRecognitionListViewModel(
            ObserveSpeechCommandGuideUseCase(simulatorRepository, guideRepository),
            ObserveManualReadoutItemSpokenUseCase(spokenRepository),
        )
}
