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
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutHotkeySettingsUseCasesTest {
    private val repository: HotkeyRepository = mockk()
    private val preferences: ReadoutHotkeyPreferencesRepository = mockk()

    @Test
    fun `キーの取得と保存と登録状態をRepositoryへ委譲する`() =
        runTest {
            every { preferences.observeVirtualKeyCode() } returns flowOf(0x70)
            val states =
                listOf(
                    HotkeyRegistrationState.Unsupported,
                    HotkeyRegistrationState.Registering,
                    HotkeyRegistrationState.Registered,
                    HotkeyRegistrationState.Conflict,
                    HotkeyRegistrationState.Failed(5),
                )
            every { repository.registrationStateStream() } returns flowOf(*states.toTypedArray())
            coEvery { preferences.saveVirtualKeyCode(0x41) } returns Unit
            coEvery { repository.retryRegistration() } returns Unit
            assertEquals(0x70, ObserveReadoutHotkeyVirtualKeyCodeUseCase(preferences)().first())
            assertEquals(states, ObserveReadoutHotkeyRegistrationStateUseCase(repository)().toList())
            assertEquals(5, (states.last() as HotkeyRegistrationState.Failed).errorCode)
            SaveReadoutHotkeyVirtualKeyCodeUseCase(preferences)(0x41)
            RetryReadoutHotkeyRegistrationUseCase(repository)()
            verify(exactly = 1) { preferences.observeVirtualKeyCode() }
            verify(exactly = 1) { repository.registrationStateStream() }
            coVerify(exactly = 1) { preferences.saveVirtualKeyCode(0x41) }
            coVerify(exactly = 1) { repository.retryRegistration() }
            confirmVerified(repository, preferences)
        }
}
