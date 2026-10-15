package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test

class UpdateManualReadoutSetUseCaseTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `更新するセットをRepositoryに渡す`() =
        runTest {
            val useCase = UpdateManualReadoutSetUseCase(repository)
            val set = ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))
            coEvery { repository.updateSet(set) } returns Unit

            useCase(set)

            coVerify(exactly = 1) { repository.updateSet(set) }
            confirmVerified(repository)
        }
}
