package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelLapsUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5RemainingFuelLapsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `燃料残り周回数を保存できる`() =
        runTest {
            SaveGt7Ps5RemainingFuelLapsUseCase(repository)(1)

            coVerify(exactly = 1) { repository.saveRemainingFuelLaps(1) }
            confirmVerified(repository)
        }
}
