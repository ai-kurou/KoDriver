package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreTemperatureReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreTemperatureColdReadoutTextUseCaseTest {
    private val repository: LmuWindowsTyreTemperatureReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsTyreTemperatureColdReadoutTextUseCase(repository)("  タイヤが冷えています  ")

            coVerify(exactly = 1) { repository.saveColdReadoutText("タイヤが冷えています") }
            confirmVerified(repository)
        }
}
