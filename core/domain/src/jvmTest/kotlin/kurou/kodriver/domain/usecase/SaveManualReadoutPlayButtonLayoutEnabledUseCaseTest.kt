package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kotlin.test.Test

class SaveManualReadoutPlayButtonLayoutEnabledUseCaseTest {
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `有効・無効のどちらも保存できる`() =
        runTest {
            val useCase = SaveManualReadoutPlayButtonLayoutEnabledUseCase(repository)

            useCase(true)
            useCase(false)

            coVerify(exactly = 1) { repository.saveManualReadoutPlayButtonLayoutEnabled(true) }
            coVerify(exactly = 1) { repository.saveManualReadoutPlayButtonLayoutEnabled(false) }
            confirmVerified(repository)
        }
}
