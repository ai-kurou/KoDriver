package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsPitTimingTyreWearLapsUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsPitTimingPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `タイヤ摩耗予想残り周回数を保存できる`() =
        runBlocking {
            SaveLmuWindowsPitTimingTyreWearLapsUseCase(repository)(2)

            coVerify(exactly = 1) { repository.saveTyreWearLaps(2) }
            confirmVerified(repository)
        }
}
