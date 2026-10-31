package kurou.kodriver.domain.gt7ps5.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCaseTest {
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(repository)("  タイヤが過熱しています  ")

            coVerify(exactly = 1) { repository.saveOverheatReadoutText("タイヤが過熱しています") }
            confirmVerified(repository)
        }

    @Test
    fun `上限ちょうどの文言と未知トークンはそのまま保存する`() =
        runTest {
            val text = "{wheel}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - "{wheel}".length)
            SaveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(repository)(text)
            coVerify(exactly = 1) { repository.saveOverheatReadoutText(text) }
            confirmVerified(repository)
        }
}
