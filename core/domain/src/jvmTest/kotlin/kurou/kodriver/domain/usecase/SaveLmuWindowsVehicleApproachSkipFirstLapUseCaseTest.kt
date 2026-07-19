package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `スキップ設定を保存できる`() = runBlocking {
        SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)(false)

        coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
        confirmVerified(repository)
    }
}
