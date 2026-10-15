package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveManualReadoutSetsUseCaseTest {
    private val repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `Repositoryが返すセットをそのまま返す`() =
        runTest {
            val sets = listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2"))
            every { repository.observeSets() } returns MutableStateFlow(sets)
            val useCase = ObserveManualReadoutSetsUseCase(repository)

            assertEquals(sets, useCase().first())
            verify(exactly = 1) { repository.observeSets() }
            confirmVerified(repository)
        }
}
