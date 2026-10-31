package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(repository)("  フルコースイエロー  ")

            coVerify(exactly = 1) { repository.saveFullCourseYellowFlagText("フルコースイエロー") }
            confirmVerified(repository)
        }
}
