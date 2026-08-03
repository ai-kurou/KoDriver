package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `任意のフェーズ集合を保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsTyreTemperatureLowWarningPhasesUseCase(repository)
            val phases = setOf(SessionPhase.WARM_UP, SessionPhase.FORMATION)

            useCase(phases)
            useCase(emptySet())

            coVerify(exactly = 1) { repository.saveLowWarningPhases(phases) }
            coVerify(exactly = 1) { repository.saveLowWarningPhases(emptySet()) }
            confirmVerified(repository)
        }
}
