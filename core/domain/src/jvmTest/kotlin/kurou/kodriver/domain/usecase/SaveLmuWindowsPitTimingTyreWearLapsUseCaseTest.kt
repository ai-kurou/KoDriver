package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingTyreWearLapsUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `タイヤ摩耗予想残り周回数を保存できる`() =
        runTest {
            SaveLmuWindowsPitTimingTyreWearLapsUseCase(repository)(2)

            coVerify(exactly = 1) { repository.saveTyreWearLaps(2) }
            confirmVerified(repository)
        }
}
