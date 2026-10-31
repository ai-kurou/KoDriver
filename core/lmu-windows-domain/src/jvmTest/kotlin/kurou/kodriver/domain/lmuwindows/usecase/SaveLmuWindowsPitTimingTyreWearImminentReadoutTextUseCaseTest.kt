package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase(repository)("  ブルー、道を譲れ  ")

            coVerify(exactly = 1) { repository.saveTyreWearImminentReadoutText("ブルー、道を譲れ") }
            confirmVerified(repository)
        }
}
