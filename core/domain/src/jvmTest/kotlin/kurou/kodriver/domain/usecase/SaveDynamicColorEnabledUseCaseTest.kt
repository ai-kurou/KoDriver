package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kotlin.test.Test

class SaveDynamicColorEnabledUseCaseTest {

    @Test
    fun `Repositoryへ保存する`() = runBlocking {
        val repository = mockk<DynamicColorEnabledRepository>(relaxUnitFun = true)

        SaveDynamicColorEnabledUseCase(repository)(true)

        coVerify(exactly = 1) { repository.saveDynamicColorEnabled(true) }
        confirmVerified(repository)
    }
}
