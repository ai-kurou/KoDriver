package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCaseTest {
    private val repository: LmuWindowsRemainingVirtualEnergyPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(repository)("  残量{percent}%  ")

            coVerify(exactly = 1) { repository.saveReadoutText("残量{percent}%") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.saveReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
