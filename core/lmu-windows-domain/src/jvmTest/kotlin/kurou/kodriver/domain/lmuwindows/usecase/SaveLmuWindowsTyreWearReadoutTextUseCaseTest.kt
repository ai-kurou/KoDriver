package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsTyreWearReadoutTextUseCaseTest {
    private val repository: LmuWindowsTyreWearPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsTyreWearReadoutTextUseCase(repository)("  残量{percent}%  ")

            coVerify(exactly = 1) { repository.saveReadoutText("残量{percent}%") }
            confirmVerified(repository)
        }
}
