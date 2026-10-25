package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingFuelLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `燃料予想残り周回数を保存できる`() =
        runTest {
            SaveLmuWindowsPitTimingFuelLapsUseCase(repository)(2)

            coVerify(exactly = 1) { repository.saveFuelLaps(2) }
            confirmVerified(repository)
        }
}
