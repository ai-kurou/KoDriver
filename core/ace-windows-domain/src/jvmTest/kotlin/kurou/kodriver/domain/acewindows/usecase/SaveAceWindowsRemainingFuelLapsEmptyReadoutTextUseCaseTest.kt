package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository)("  燃料なし  ")

            coVerify(exactly = 1) { repository.saveEmptyReadoutText("燃料なし") }
            confirmVerified(repository)
        }
}
