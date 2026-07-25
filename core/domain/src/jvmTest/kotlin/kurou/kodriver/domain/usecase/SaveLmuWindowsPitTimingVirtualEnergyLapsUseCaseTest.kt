package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsPitTimingVirtualEnergyLapsUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsPitTimingPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `バーチャルエナジー予想残り周回数を保存できる`() = runBlocking {
        SaveLmuWindowsPitTimingVirtualEnergyLapsUseCase(repository)(1)

        coVerify(exactly = 1) { repository.saveVirtualEnergyLaps(1) }
        confirmVerified(repository)
    }
}
