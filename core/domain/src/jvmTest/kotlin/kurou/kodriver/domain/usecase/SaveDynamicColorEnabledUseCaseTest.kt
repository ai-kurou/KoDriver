package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kotlin.test.Test

class SaveDynamicColorEnabledUseCaseTest {
    private val repository: DynamicColorEnabledRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Repositoryへ保存する`() =
        runTest {
            SaveDynamicColorEnabledUseCase(repository)(true)

            coVerify(exactly = 1) { repository.saveDynamicColorEnabled(true) }
            confirmVerified(repository)
        }
}
