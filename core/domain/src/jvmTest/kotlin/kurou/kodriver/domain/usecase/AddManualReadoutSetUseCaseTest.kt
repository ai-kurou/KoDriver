package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test

class AddManualReadoutSetUseCaseTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `追加するセットをRepositoryに渡す`() =
        runTest {
            val useCase = AddManualReadoutSetUseCase(repository)
            val set = ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))
            coEvery { repository.addSet(set) } returns Unit

            useCase(set)

            coVerify(exactly = 1) { repository.addSet(set) }
            confirmVerified(repository)
        }
}
