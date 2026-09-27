package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRemainingFuelThresholdPercentageUseCaseTest {
    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `燃料残量閾値を保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelThresholdPercentageUseCase(repository)(45)

            coVerify(exactly = 1) { repository.saveThresholdPercentage(45) }
            confirmVerified(repository)
        }
}
