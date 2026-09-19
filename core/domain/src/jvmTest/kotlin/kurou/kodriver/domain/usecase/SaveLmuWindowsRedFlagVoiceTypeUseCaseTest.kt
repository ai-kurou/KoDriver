package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.RedFlagVoiceType
import kurou.kodriver.domain.repository.LmuWindowsRedFlagPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsRedFlagVoiceTypeUseCaseTest {
    private val repository: LmuWindowsRedFlagPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `赤旗音声タイプを保存する`() =
        runTest {
            SaveLmuWindowsRedFlagVoiceTypeUseCase(repository)(RedFlagVoiceType.RED_FLAG)

            coVerify(exactly = 1) { repository.saveVoiceType(RedFlagVoiceType.RED_FLAG) }
            confirmVerified(repository)
        }
}
