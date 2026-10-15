package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveManualReadoutRequestsUseCaseTest {
    private val repository: ManualReadoutRequestRepository = mockk()

    @Test
    fun `依頼を順番通りに通知する`() =
        runTest {
            val first = ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")
            val second = ManualReadoutRequest(Simulator.Gt7Ps5.id, "set-2")
            every { repository.observeRequests() } returns flowOf(first, second)

            assertEquals(listOf(first, second), ObserveManualReadoutRequestsUseCase(repository)().toList())

            verify(exactly = 1) { repository.observeRequests() }
            confirmVerified(repository)
        }

    @Test
    fun `通知のないフローは空のまま返す`() =
        runTest {
            every { repository.observeRequests() } returns emptyFlow()

            assertEquals(emptyList(), ObserveManualReadoutRequestsUseCase(repository)().toList())

            verify(exactly = 1) { repository.observeRequests() }
            confirmVerified(repository)
        }
}
