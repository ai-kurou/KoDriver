package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kotlin.test.Test

class SaveAceWindowsMyBestLapVoiceTypeUseCaseTest {
    private val repository: AceWindowsMyBestLapPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `ACE自己ベストラップ音声タイプを保存する`() =
        runTest {
            SaveAceWindowsMyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }
}
