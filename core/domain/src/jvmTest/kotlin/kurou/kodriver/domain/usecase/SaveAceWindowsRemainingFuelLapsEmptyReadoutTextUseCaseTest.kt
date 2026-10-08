package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsRemainingFuelLapsPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCaseTest {
    private val repository: AceWindowsRemainingFuelLapsPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository)("  燃料なし  ")

            coVerify(exactly = 1) { repository.saveEmptyReadoutText("燃料なし") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.saveEmptyReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveEmptyReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
