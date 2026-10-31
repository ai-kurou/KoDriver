package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsMyBestLapPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveLmuWindowsMyBestLapReadoutTextUseCaseTest {
    private val repository: LmuWindowsMyBestLapPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsMyBestLapReadoutTextUseCase(repository)("  更新{laptime}  ")

            coVerify(exactly = 1) { repository.saveReadoutText("更新{laptime}") }
            confirmVerified(repository)
        }

    @Test
    fun `上限ちょうどの文言と未知トークンはそのまま保存する`() =
        runTest {
            val text = "{wheel}" + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH - "{wheel}".length)
            SaveLmuWindowsMyBestLapReadoutTextUseCase(repository)(text)
            coVerify(exactly = 1) { repository.saveReadoutText(text) }
            confirmVerified(repository)
        }
}
