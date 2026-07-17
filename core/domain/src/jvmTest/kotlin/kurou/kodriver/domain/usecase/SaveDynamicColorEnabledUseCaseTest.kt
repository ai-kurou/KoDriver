package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kotlin.test.Test

class SaveDynamicColorEnabledUseCaseTest {

    @Test
    fun `Repositoryへ保存する`() = runBlocking {
        val repository = mockk<DynamicColorEnabledRepository>()
        coEvery { repository.saveDynamicColorEnabled(any()) } returns Unit

        SaveDynamicColorEnabledUseCase(repository)(true)

        coVerify { repository.saveDynamicColorEnabled(true) }
    }
}
