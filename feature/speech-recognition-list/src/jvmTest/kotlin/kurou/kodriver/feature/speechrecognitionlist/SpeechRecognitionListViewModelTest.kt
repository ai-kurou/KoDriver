package kurou.kodriver.feature.speechrecognitionlist

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository
import kurou.kodriver.domain.usecase.ObserveSpeechCommandGuideUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val guideRepository: SpeechCommandGuideRepository = mockk()

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
            val viewModel =
                SpeechRecognitionListViewModel(ObserveSpeechCommandGuideUseCase(simulatorRepository, guideRepository))
            try {
                assertEquals(
                    SpeechRecognitionListUiState(groups),
                    viewModel.uiState.first { it.groups.isNotEmpty() },
                )
                verify(exactly = 1) { simulatorRepository.selectedSimulator() }
                verify(exactly = 1) { guideRepository.guideGroups(Simulator.LmuWindows) }
                confirmVerified(simulatorRepository, guideRepository)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }
}
