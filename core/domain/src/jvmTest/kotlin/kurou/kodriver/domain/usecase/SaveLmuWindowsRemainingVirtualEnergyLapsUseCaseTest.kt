package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRemainingVirtualEnergyLapsUseCaseTest {

    @Test
    fun `バーチャルエナジー残り周回数を保存できる`() = runBlocking {
        val repository = mockk<LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository)(1)

        coVerify(exactly = 1) { repository.saveRemainingVirtualEnergyLaps(1) }
        confirmVerified(repository)
    }
}
