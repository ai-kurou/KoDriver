package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `スキップ設定を保存できる`() =
        runTest {
            SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)(false)

            coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
            confirmVerified(repository)
        }
}
