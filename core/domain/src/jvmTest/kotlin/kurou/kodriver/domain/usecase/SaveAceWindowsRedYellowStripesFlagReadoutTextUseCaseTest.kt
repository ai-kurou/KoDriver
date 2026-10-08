package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRedYellowStripesFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(repository)("  レッド・イエローストライプフラッグ、路面が滑りやすいです  ")

            coVerify(exactly = 1) {
                repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "レッド・イエローストライプフラッグ、路面が滑りやすいです")
            }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveText(
                    AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES,
                    "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH),
                )
            }
            confirmVerified(repository)
        }
}
