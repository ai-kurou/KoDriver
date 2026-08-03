package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsRemainingFuelThresholdPercentageUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: AceWindowsRemainingFuelPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `燃料残量閾値を保存する`() =
        runBlocking {
            SaveAceWindowsRemainingFuelThresholdPercentageUseCase(repository)(45)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
            confirmVerified(repository)
        }
}
