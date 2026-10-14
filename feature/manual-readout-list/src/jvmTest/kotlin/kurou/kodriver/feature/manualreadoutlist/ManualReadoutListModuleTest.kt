package kurou.kodriver.feature.manualreadoutlist

import androidx.lifecycle.viewModelScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutListModuleTest {
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
                        manualReadoutListModule,
                        module {
                            repositories.forEach { (id, repository) ->
                                single<ManualReadoutSetPreferencesRepository>(named(id)) { repository }
                            }
                        },
                    )
                }
            try {
                Simulator.entries.forEach { simulator ->
                    val viewModel = application.koin.get<ManualReadoutListViewModel> { parametersOf(simulator.id) }
                    val addedSet = slot<ManualReadoutSet>()
                    val repository = repositories.getValue(simulator.id)
                    coEvery { repository.addSet(capture(addedSet)) } returns Unit
                    try {
                        val state = viewModel.uiState.first { it.sets.isNotEmpty() }
                        assertEquals(listOf(ManualReadoutSet(id = simulator.id)), state.sets)
                        viewModel.addSet()
                        val saved = addedSet.captured
                        assertEquals(emptyList(), saved.itemKeys)
                        assertNotEquals(simulator.id, saved.id)
                        assertEquals(
                            saved.id,
                            java.util.UUID
                                .fromString(saved.id)
                                .toString(),
                        )
                        coVerify(exactly = 1) { repository.addSet(saved) }
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
