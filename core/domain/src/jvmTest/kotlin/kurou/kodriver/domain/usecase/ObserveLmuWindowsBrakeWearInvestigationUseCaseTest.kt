package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsBrakeWearInvestigationUseCaseTest {
    private val repo: LmuWindowsBrakeWearInvestigationRepository = mockk()

    @Test
    fun `invoke はリポジトリの investigationStream を順番通りに流す`() =
        runTest {
            val data1 = LmuWindowsBrakeWearInvestigationData(wearablesBrakes = listOf(0.036, 0.035, 0.032, 0.031))
            val data2 = LmuWindowsBrakeWearInvestigationData(brakeInfo = listOf(0.036, 0.036, 0.032, 0.032))
            every { repo.investigationStream() } returns flowOf(data1, data2)
            val useCase = ObserveLmuWindowsBrakeWearInvestigationUseCase(repo)

            val results = buildList { useCase().collect { add(it) } }

            assertEquals(listOf(data1, data2), results)
            verify(exactly = 1) { repo.investigationStream() }
            confirmVerified(repo)
        }
}
