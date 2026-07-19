package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.Test

class SaveGt7Ps5MyBestLapVoiceTypeUseCaseTest {

    @Test
    fun `音声タイプを保存できる`() = runBlocking {
        val repository = mockk<Gt7Ps5MyBestLapPreferencesRepository>(relaxUnitFun = true)

        SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

        coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
        confirmVerified(repository)
    }
}
