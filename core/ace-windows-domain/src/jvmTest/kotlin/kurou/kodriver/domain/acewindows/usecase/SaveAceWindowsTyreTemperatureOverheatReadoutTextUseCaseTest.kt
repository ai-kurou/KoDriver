package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCaseTest {
    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsTyreTemperatureOverheatReadoutTextUseCase(repository)("  タイヤが過熱しています  ")

            coVerify(exactly = 1) { repository.saveOverheatReadoutText("タイヤが過熱しています") }
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
