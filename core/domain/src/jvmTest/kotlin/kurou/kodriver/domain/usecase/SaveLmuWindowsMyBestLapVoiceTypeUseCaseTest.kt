package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsMyBestLapVoiceTypeUseCaseTest {
    private val repository: LmuWindowsMyBestLapPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `LMU自己ベストラップ音声タイプを保存する`() =
        runTest {
            SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }
}
