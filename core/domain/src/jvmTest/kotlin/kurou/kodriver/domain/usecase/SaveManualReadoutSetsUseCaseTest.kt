package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test

class SaveManualReadoutSetsUseCaseTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `保存すると上書きで更新される`() =
        runTest {
            val useCase = SaveManualReadoutSetsUseCase(repository)
            val firstSets = listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2"))
            val secondSets =
                listOf(ManualReadoutSet("set-1", listOf("SIMULATOR", "SESSION")), ManualReadoutSet("set-2"))

            coEvery { repository.saveSets(firstSets) } returns Unit
            coEvery { repository.saveSets(secondSets) } returns Unit

            useCase(firstSets)
            useCase(secondSets)

            coVerify(exactly = 1) { repository.saveSets(firstSets) }
            coVerify(exactly = 1) { repository.saveSets(secondSets) }
            confirmVerified(repository)
        }
}
