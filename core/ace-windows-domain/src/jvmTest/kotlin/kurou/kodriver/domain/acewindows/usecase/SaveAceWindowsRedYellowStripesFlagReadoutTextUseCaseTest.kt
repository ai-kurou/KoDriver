package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsRedYellowStripesFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsRedYellowStripesFlagReadoutTextUseCase(repository)("  レッド・イエローストライプフラッグ、路面が滑りやすいです  ")

            coVerify(exactly = 1) {
                repository.saveText(AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES, "レッド・イエローストライプフラッグ、路面が滑りやすいです")
            }
            confirmVerified(repository)
        }
}
