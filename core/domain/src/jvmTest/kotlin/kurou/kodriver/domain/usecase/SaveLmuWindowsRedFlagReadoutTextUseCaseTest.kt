package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRedFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsRedFlagReadoutTextUseCase(repository)("  レッドフラッグ、セッション中断  ")

            coVerify(exactly = 1) { repository.saveRedFlagText("レッドフラッグ、セッション中断") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsRedFlagReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveRedFlagText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveLmuWindowsRedFlagReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveRedFlagText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
