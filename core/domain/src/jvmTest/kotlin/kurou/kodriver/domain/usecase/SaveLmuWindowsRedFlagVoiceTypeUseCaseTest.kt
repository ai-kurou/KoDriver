package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsRedFlagVoiceTypeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsRedFlagPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `赤旗音声タイプを保存する`() =
        runTest {
            SaveLmuWindowsRedFlagVoiceTypeUseCase(repository)(RedFlagVoiceType.RED_FLAG)

            coVerify(exactly = 1) { repository.saveVoiceType(RedFlagVoiceType.RED_FLAG) }
            confirmVerified(repository)
        }
}
