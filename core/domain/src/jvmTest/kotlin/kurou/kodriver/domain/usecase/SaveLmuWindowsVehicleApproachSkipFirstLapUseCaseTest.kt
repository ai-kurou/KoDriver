package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleApproachSkipFirstLapUseCaseTest {

    @Test
    fun `スキップ設定を保存できる`() = runBlocking {
        val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsVehicleApproachSkipFirstLapUseCase(repository)(false)

        coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
        confirmVerified(repository)
    }
}
