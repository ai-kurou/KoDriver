package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsOverheatVoiceTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsOverheatPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `オーバーヒート音声タイプを保存する`() =
        runTest {
            SaveLmuWindowsOverheatVoiceTypeUseCase(repository)(OverheatVoiceType.STANDARD)

            coVerify(exactly = 1) { repository.saveVoiceType(OverheatVoiceType.STANDARD) }
            confirmVerified(repository)
        }
}
