package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsSectorYellowFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(repository)("  イエロー、注意  ")

            coVerify(exactly = 1) { repository.saveSectorYellowFlagText("イエロー、注意") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveSectorYellowFlagText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveSectorYellowFlagText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
