package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsMyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `LMU自己ベストラップ音声タイプを保存する`() = runBlocking {
        val repository = mockk<LmuWindowsMyBestLapPreferencesRepository>(relaxUnitFun = true)

        SaveLmuWindowsMyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

        coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
        confirmVerified(repository)
    }
}
