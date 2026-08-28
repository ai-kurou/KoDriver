package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import kurou.kodriver.domain.repository.AceWindowsMyBestLapPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveAceWindowsMyBestLapVoiceTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: AceWindowsMyBestLapPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `ACE自己ベストラップ音声タイプを保存する`() =
        runTest {
            SaveAceWindowsMyBestLapVoiceTypeUseCase(repository)(MyBestLapVoiceType.CASUAL)

            coVerify(exactly = 1) { repository.saveVoiceType(MyBestLapVoiceType.CASUAL) }
            confirmVerified(repository)
        }
}
