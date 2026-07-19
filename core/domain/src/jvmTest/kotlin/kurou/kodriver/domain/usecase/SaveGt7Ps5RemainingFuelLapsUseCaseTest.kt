package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5RemainingFuelLapsUseCaseTest {

    @Test
    fun `燃料残り周回数を保存できる`() = runBlocking {
        val repository = mockk<Gt7Ps5RemainingFuelLapsPreferencesRepository>(relaxUnitFun = true)

        SaveGt7Ps5RemainingFuelLapsUseCase(repository)(1)

        coVerify(exactly = 1) { repository.saveRemainingFuelLaps(1) }
        confirmVerified(repository)
    }
}
