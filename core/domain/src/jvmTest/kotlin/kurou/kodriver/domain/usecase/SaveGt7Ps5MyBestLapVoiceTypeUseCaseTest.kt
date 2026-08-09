package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.Gt7Ps5MyBestLapPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveGt7Ps5MyBestLapVoiceTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: Gt7Ps5MyBestLapPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `音声タイプを保存できる`() =
        runTest {
            SaveGt7Ps5MyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }
}
