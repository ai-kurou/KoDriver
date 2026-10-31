package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveAceWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)("  残り{percent}パーセント  ")

            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}パーセント") }
            confirmVerified(repository)
        }

    @Test
    fun `上限ちょうどの文言と未知トークンはそのまま保存する`() =
        runTest {
            val text = "{lap}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - "{lap}".length)
            SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)(text)
            coVerify(exactly = 1) { repository.saveReadoutText(text) }
            confirmVerified(repository)
        }
}
