package kurou.kodriver.feature.manualreadoutsetdetail

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetDetailModuleTest {
    private val debugRepository: ReadoutHotkeyPreferencesRepository = mockk()
    private val dispatcher = UnconfinedTestDispatcher()
    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()
    private val aceRepository: ManualReadoutSetPreferencesRepository = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `全シミュレーターのパラメータで解決したViewModelは対応するRepositoryのセットを返す`() =
        runTest(dispatcher) {
            val repositories =
                mapOf(
                    Simulator.LmuWindows.id to lmuRepository,
                    Simulator.Gt7Ps5.id to gt7Repository,
                    Simulator.AceWindows.id to aceRepository,
                )
            repositories.forEach { (id, repository) ->
                every { repository.observeSets() } returns flowOf(listOf(ManualReadoutSet(id = id)))
            }
            val application =
                koinApplication {
                    modules(
                        manualReadoutSetDetailModule,
                        module {
                            single<ReadoutHotkeyPreferencesRepository> { debugRepository }
                            repositories.forEach { (id, repository) ->
                                single<ManualReadoutSetPreferencesRepository>(named(id)) { repository }
                            }
                        },
                    )
                }
            try {
                Simulator.entries.forEach { simulator ->
                    val viewModel =
                        application.koin.get<ManualReadoutSetDetailViewModel> {
                            parametersOf(simulator.id, simulator.id)
                        }
                    val repository = repositories.getValue(simulator.id)
                    coEvery { repository.deleteSet(simulator.id) } returns Unit
                    try {
                        val state = viewModel.uiState.first { !it.isLoading }
                        assertEquals(1, state.setNumber)
                        assertEquals(emptyList(), state.selectedItemKeys)
                        assertEquals(manualReadoutItemKeys(simulator.id), state.availableItemKeys)
                        viewModel.deleteSet()
                        assertEquals(true, viewModel.uiState.first { it.deleted }.deleted)
                        coVerify(exactly = 1) { repository.deleteSet(simulator.id) }
                    } finally {
                        viewModel.viewModelScope.cancel()
                    }
                }
                repositories.values.forEach { repository ->
                    verify(exactly = 1) { repository.observeSets() }
                }
                confirmVerified(lmuRepository, gt7Repository, aceRepository)
            } finally {
                application.close()
            }
        }
}
