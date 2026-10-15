package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test

class DeleteManualReadoutSetUseCaseTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `削除するセットをRepositoryに渡す`() =
        runTest {
            val useCase = DeleteManualReadoutSetUseCase(repository)
            val id = "set-1"
            coEvery { repository.deleteSet(id) } returns Unit

            useCase(id)

            coVerify(exactly = 1) { repository.deleteSet(id) }
            confirmVerified(repository)
        }
}
