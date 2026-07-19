package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsRemainingVirtualEnergyLapsUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsRemainingVirtualEnergyLapsPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `バーチャルエナジー残り周回数を保存できる`() = runBlocking {
        SaveLmuWindowsRemainingVirtualEnergyLapsUseCase(repository)(1)

        coVerify(exactly = 1) { repository.saveRemainingVirtualEnergyLaps(1) }
        confirmVerified(repository)
    }
}
