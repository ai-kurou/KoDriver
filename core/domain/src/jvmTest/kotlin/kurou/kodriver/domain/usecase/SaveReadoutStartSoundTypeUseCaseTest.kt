package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveReadoutStartSoundTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: ReadoutStartSoundPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `読み上げ開始音種別を保存できる`() =
        runBlocking {
            SaveReadoutStartSoundTypeUseCase(repository)(ReadoutStartSoundType.FORMULA_RADIO)

            coVerify(exactly = 1) { repository.saveType(ReadoutStartSoundType.FORMULA_RADIO) }
            confirmVerified(repository)
        }
}
