package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsCheckeredFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveAceWindowsCheckeredFlagReadoutTextUseCase(repository)("  チェッカー、完走  ")

            coVerify(exactly = 1) { repository.saveCheckeredFlagText("チェッカー、完走") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveAceWindowsCheckeredFlagReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveCheckeredFlagText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveAceWindowsCheckeredFlagReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveCheckeredFlagText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
