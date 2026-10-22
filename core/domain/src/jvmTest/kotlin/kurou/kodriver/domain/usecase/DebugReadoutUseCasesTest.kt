package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DebugReadoutRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.DebugReadoutRequestRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugReadoutUseCasesTest {
    private val repository: DebugReadoutRequestRepository = mockk()
    private val request =
        DebugReadoutRequest(
            Simulator.LmuWindows,
            object : kurou.kodriver.domain.model.ReadoutItemKey.TopLevel {
                override val value = "test"
                override val supportsQueue = true
            },
            " {laps} ",
        )

    @Test
    fun `入力をそのまま送信する`() =
        runTest {
            coEvery { repository.requestReadout(request) } returns Unit
            RequestDebugReadoutUseCase(repository)(request)
            coVerify(exactly = 1) { repository.requestReadout(request) }
            confirmVerified(repository)
        }

    @Test
    fun `要求Flowをそのまま購読する`() =
        runTest {
            every { repository.observeRequests() } returns flowOf(request)
            assertEquals(request, ObserveDebugReadoutRequestUseCase(repository)().first())
            verify(exactly = 1) { repository.observeRequests() }
            confirmVerified(repository)
        }
}
