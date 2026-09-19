package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsOverheatVoiceTypeUseCaseTest {
    private val repository: LmuWindowsOverheatPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `オーバーヒート音声タイプを保存する`() =
        runTest {
            SaveLmuWindowsOverheatVoiceTypeUseCase(repository)(OverheatVoiceType.STANDARD)

            coVerify(exactly = 1) { repository.saveVoiceType(OverheatVoiceType.STANDARD) }
            confirmVerified(repository)
        }
}
