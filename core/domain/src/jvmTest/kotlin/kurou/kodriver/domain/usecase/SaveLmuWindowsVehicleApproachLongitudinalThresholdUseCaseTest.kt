package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachThresholdsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachThresholdsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `縦方向閾値を保存するとFlowに反映され上書きで更新される`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleApproachLongitudinalThresholdUseCase(repository)

            useCase(50.0)
            useCase(30.0)

            coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(50.0) }
            coVerify(exactly = 1) { repository.saveLongitudinalThresholdMeters(30.0) }
            confirmVerified(repository)
        }
}
