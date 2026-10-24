package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveLmuWindowsRemainingFuelReadoutTextUseCaseTest {
    private val repository: LmuWindowsRemainingFuelPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository)("  燃料{percent}%  ")
            coVerify(exactly = 1) { repository.saveReadoutText("燃料{percent}%") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository)(" \t\n ")
            coVerify(exactly = 1) { repository.saveReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "
            SaveLmuWindowsRemainingFuelReadoutTextUseCase(repository)(text)
            coVerify(exactly = 1) { repository.saveReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH)) }
            confirmVerified(repository)
        }
}
