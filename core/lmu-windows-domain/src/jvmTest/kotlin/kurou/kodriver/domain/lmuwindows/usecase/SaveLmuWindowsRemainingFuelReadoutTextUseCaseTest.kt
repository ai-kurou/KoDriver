package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository)("  燃料{percent}%  ")
            coVerify(exactly = 1) { repository.saveReadoutText("燃料{percent}%") }
            confirmVerified(repository)
        }
}
