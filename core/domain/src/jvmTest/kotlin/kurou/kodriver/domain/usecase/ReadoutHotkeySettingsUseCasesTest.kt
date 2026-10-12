package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.HotkeyRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutHotkeySettingsUseCasesTest {
    private val repository: HotkeyRepository = mockk()

    @Test
    fun `キーの取得と保存と登録状態をRepositoryへ委譲する`() =
        runTest {
            every { repository.observeVirtualKeyCode() } returns flowOf(0x70)
            val states =
                listOf(
                    HotkeyRegistrationState.Unsupported,
                    HotkeyRegistrationState.Registering,
                    HotkeyRegistrationState.Registered,
                    HotkeyRegistrationState.Conflict,
                    HotkeyRegistrationState.Failed(5),
                )
            every { repository.registrationStateStream() } returns flowOf(*states.toTypedArray())
            coEvery { repository.saveVirtualKeyCode(0x41) } returns Unit
            assertEquals(0x70, ObserveReadoutHotkeyVirtualKeyCodeUseCase(repository)().first())
            assertEquals(states, ObserveReadoutHotkeyRegistrationStateUseCase(repository)().toList())
            assertEquals(5, (states.last() as HotkeyRegistrationState.Failed).errorCode)
            SaveReadoutHotkeyVirtualKeyCodeUseCase(repository)(0x41)
            verify(exactly = 1) { repository.observeVirtualKeyCode() }
            verify(exactly = 1) { repository.registrationStateStream() }
            coVerify(exactly = 1) { repository.saveVirtualKeyCode(0x41) }
            confirmVerified(repository)
        }
}
