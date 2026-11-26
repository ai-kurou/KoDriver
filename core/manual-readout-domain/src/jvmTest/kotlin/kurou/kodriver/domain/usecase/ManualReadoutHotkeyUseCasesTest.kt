package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ManualReadoutHotkeyUseCasesTest {
    private val repository: ManualReadoutHotkeyRepository = mockk()

    @Test
    fun `押下と状態の購読と再試行をセット識別子とともに委譲する`() =
        runTest {
            val request = ManualReadoutRequest("lmu_windows", "set-1")
            val states = listOf(HotkeyRegistrationState.Registering, HotkeyRegistrationState.Failed(5))
            every { repository.pressStream() } returns flowOf(request)
            every { repository.registrationStateStream("lmu_windows", "set-1") } returns flowOf(*states.toTypedArray())
            coEvery { repository.retryRegistration("lmu_windows", "set-1") } returns Unit
            assertEquals(listOf(request), ObserveManualReadoutHotkeyPressesUseCase(repository)().toList())
            assertEquals(
                states,
                ObserveManualReadoutHotkeyRegistrationStateUseCase(repository)("lmu_windows", "set-1").toList(),
            )
            RetryManualReadoutHotkeyRegistrationUseCase(repository)("lmu_windows", "set-1")
            verify(exactly = 1) { repository.pressStream() }
            verify(exactly = 1) { repository.registrationStateStream("lmu_windows", "set-1") }
            coVerify(exactly = 1) { repository.retryRegistration("lmu_windows", "set-1") }
            confirmVerified(repository)
        }
}
