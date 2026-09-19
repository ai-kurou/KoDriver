package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5MyBestLapVoiceTypeUseCaseTest {
    private val repository: Gt7Ps5MyBestLapPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `音声タイプを保存できる`() =
        runTest {
            SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }
}
