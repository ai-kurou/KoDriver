package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.LmuWindowsTyreWearPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsTyreWearThresholdPercentageUseCaseTest {

    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsTyreWearPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `任意の値を保存できる`() = runBlocking {
        val useCase = SaveLmuWindowsTyreWearThresholdPercentageUseCase(repository)

        useCase(30)
        useCase(60)

        coVerify(exactly = 1) { repository.saveThresholdPercentage(30) }
        coVerify(exactly = 1) { repository.saveThresholdPercentage(60) }
        confirmVerified(repository)
    }
}
