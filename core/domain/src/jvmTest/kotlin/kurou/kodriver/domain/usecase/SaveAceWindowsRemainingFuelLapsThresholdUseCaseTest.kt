package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRemainingFuelLapsThresholdUseCaseTest {
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `燃料残り周回数を保存できる`() =
        runTest {
            SaveAceWindowsRemainingFuelLapsThresholdUseCase(repository)(1)

            coVerify(exactly = 1) { repository.saveThresholdLaps(1) }
            confirmVerified(repository)
        }
}
