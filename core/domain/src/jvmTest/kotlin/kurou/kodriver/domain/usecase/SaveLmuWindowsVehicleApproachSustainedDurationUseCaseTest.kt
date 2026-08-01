package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSustainedDurationUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存した継続時間閾値がFlowに反映される`() =
        runBlocking {
            SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repository)(8)

            coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(8) }
            confirmVerified(repository)
        }

    @Test
    fun `上書き保存すると最新値がFlowに反映される`() =
        runBlocking {
            val useCase = SaveLmuWindowsVehicleApproachSustainedDurationUseCase(repository)

            useCase(8)
            useCase(6)

            coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(8) }
            coVerify(exactly = 1) { repository.saveSustainedApproachDurationSeconds(6) }
            confirmVerified(repository)
        }
}
