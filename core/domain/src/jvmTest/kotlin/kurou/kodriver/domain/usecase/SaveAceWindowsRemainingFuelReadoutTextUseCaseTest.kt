package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)("  残り{percent}パーセント  ")

            coVerify(exactly = 1) { repository.saveReadoutText("残り{percent}パーセント") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.saveReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveAceWindowsRemainingFuelReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
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
