package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachLateralThresholdUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `横方向閾値を保存するとFlowに反映され上書きで更新される`() =
        runBlocking {
            val useCase = SaveLmuWindowsVehicleApproachLateralThresholdUseCase(repository)

            useCase(3.5)
            useCase(1.0)

            coVerify(exactly = 1) { repository.saveLateralThresholdMeters(3.5) }
            coVerify(exactly = 1) { repository.saveLateralThresholdMeters(1.0) }
            confirmVerified(repository)
        }
}
