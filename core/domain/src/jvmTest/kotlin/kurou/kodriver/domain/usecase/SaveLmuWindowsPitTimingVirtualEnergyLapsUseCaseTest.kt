package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingVirtualEnergyLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `バーチャルエナジー予想残り周回数を保存できる`() =
        runTest {
            SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository)(1)

            coVerify(exactly = 1) { repository.saveVirtualEnergyLaps(1) }
            confirmVerified(repository)
        }
}
