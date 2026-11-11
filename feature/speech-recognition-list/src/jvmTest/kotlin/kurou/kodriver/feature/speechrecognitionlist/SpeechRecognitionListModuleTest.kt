package kurou.kodriver.feature.speechrecognitionlist

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
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
import kurou.kodriver.domain.repository.ManualReadoutItemSpokenRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechRecognitionListModuleTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val guideRepository: SpeechCommandGuideRepository = mockk()
    private val spokenRepository: ManualReadoutItemSpokenRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ViewModelは登録済みのRepositoryから案内を取得する`() =
        runTest(dispatcher) {
            val groups =
                listOf(
                    SpeechCommandGuideGroup(
                        ManualReadoutItemCategory.LAP,
                        listOf(SpeechCommandGuideItem("lap", "ベストラップ", listOf("ベスト"))),
                    ),
                )
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.Gt7Ps5)
            every { guideRepository.guideGroups(Simulator.Gt7Ps5) } returns groups
            every { spokenRepository.observeSpoken() } returns emptyFlow()
            val application =
                koinApplication {
                    modules(
                        speechRecognitionListModule,
                        module {
                            single<SimulatorPreferencesRepository> { simulatorRepository }
                            single<SpeechCommandGuideRepository> { guideRepository }
                            single<ManualReadoutItemSpokenRepository> { spokenRepository }
                        },
                    )
                }
            val viewModel = application.koin.get<SpeechRecognitionListViewModel>()
            try {
                assertEquals(groups, viewModel.uiState.first { it.groups.isNotEmpty() }.groups)
                verify(exactly = 1) { simulatorRepository.selectedSimulator() }
                verify(exactly = 1) { guideRepository.guideGroups(Simulator.Gt7Ps5) }
                verify(exactly = 1) { spokenRepository.observeSpoken() }
                confirmVerified(simulatorRepository, guideRepository, spokenRepository)
            } finally {
                viewModel.viewModelScope.cancel()
                application.close()
            }
        }
}
