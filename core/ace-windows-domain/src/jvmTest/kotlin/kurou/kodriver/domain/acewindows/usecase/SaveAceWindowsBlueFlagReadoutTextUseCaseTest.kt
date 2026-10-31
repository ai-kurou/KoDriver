package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsBlueFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `正規化した文言を保存する`() =
        runTest {
            SaveAceWindowsBlueFlagReadoutTextUseCase(repository)("  ブルーフラッグ  ")

            coVerify(exactly = 1) { repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "ブルーフラッグ") }
            confirmVerified(repository)
        }
}
