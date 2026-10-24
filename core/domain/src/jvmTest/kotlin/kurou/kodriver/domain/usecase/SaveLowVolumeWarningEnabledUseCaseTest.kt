package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kotlin.test.Test

class SaveLowVolumeWarningEnabledUseCaseTest {
    private val repository: LowVolumeWarningEnabledPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `有効・無効のどちらも保存できる`() =
        runTest {
            val useCase = SaveLowVolumeWarningEnabledUseCase(repository)

            useCase(true)
            useCase(false)

            coVerify(exactly = 1) { repository.saveLowVolumeWarningEnabled(true) }
            coVerify(exactly = 1) { repository.saveLowVolumeWarningEnabled(false) }
            confirmVerified(repository)
        }
}
