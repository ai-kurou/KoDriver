package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.lmuWindowsTyreTemperatureLowWarningSelectablePhases
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(
    repository: LmuWindowsTyreTemperaturePreferencesRepository,
    initialLowWarningPhases: Map<SessionPhase, Boolean> = emptyMap(),
): LmuWindowsTyreTemperaturePreferencesRepository {
    val lowWarningPhases = MutableStateFlow(initialLowWarningPhases)
    every { repository.observeLowWarningPhases() } returns lowWarningPhases
    listOf(setOf(SessionPhase.GARAGE)).forEach { phases ->
        coEvery { repository.saveLowWarningPhases(phases) } answers {
            lowWarningPhases.update {
                lmuWindowsTyreTemperatureLowWarningSelectablePhases.associateWith { phase -> phase in phases }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {
    @MockK
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runBlocking {
            val repo = createLmuWindowsTyreTemperaturePreferencesRepository(repository)
            val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

            assertEquals(
                setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
                useCase().first(),
            )

            repo.saveLowWarningPhases(setOf(SessionPhase.GARAGE))
            assertEquals(setOf(SessionPhase.GARAGE), useCase().first())
            coVerify(exactly = 1) { repo.saveLowWarningPhases(setOf(SessionPhase.GARAGE)) }
            verify(exactly = 2) { repo.observeLowWarningPhases() }
            confirmVerified(repo)
        }

    @Test
    fun `一部のフェーズだけ保存されている場合は保存済みの値がデフォルトより優先される`() =
        runBlocking {
            val repo =
                createLmuWindowsTyreTemperaturePreferencesRepository(
                    repository,
                    initialLowWarningPhases = mapOf(SessionPhase.GARAGE to true),
                )
            val useCase = ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repo)

            assertEquals(
                setOf(SessionPhase.GARAGE, SessionPhase.WARM_UP, SessionPhase.GRID_WALK, SessionPhase.FORMATION),
                useCase().first(),
            )
            verify(exactly = 1) { repo.observeLowWarningPhases() }
            confirmVerified(repo)
        }
}
