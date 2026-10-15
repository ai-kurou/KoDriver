package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kotlin.test.Test

class RequestManualReadoutUseCaseTest {
    private val repository: ManualReadoutRequestRepository = mockk()

    @Test
    fun `シミュレーターとセットを指定した依頼をそのまま渡す`() =
        runTest {
            val request = ManualReadoutRequest(Simulator.LmuWindows.id, "set-1")
            coEvery { repository.request(request) } returns Unit

            RequestManualReadoutUseCase(repository)(request)

            coVerify(exactly = 1) { repository.request(request) }
            confirmVerified(repository)
        }
}
