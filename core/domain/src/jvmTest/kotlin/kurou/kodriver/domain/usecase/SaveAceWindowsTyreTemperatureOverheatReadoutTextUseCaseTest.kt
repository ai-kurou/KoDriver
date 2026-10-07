package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCaseTest {
    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)("  タイヤが過熱しています  ")

            coVerify(exactly = 1) { repository.saveOverheatReadoutText("タイヤが過熱しています") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.saveOverheatReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveOverheatReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }

    @Test
    fun `上限ちょうどの文言と未知トークンはそのまま保存する`() =
        runTest {
            val text = "{wheel}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - "{wheel}".length)
            SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)(text)
            coVerify(exactly = 1) { repository.saveOverheatReadoutText(text) }
            confirmVerified(repository)
        }
}
