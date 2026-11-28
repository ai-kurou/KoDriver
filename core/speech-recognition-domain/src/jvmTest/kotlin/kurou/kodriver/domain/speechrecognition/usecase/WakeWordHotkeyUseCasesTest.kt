package kurou.kodriver.domain.speechrecognition.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class WakeWordHotkeyUseCasesTest {
    private val preferences = mockk<WakeWordHotkeyPreferencesRepository>()
    private val repository = mockk<WakeWordHotkeyRepository>()

    @Test
    fun `割り当ての購読はRepositoryのFlowを返す`() {
        val flow = flowOf(WakeWordHotkey())
        every { preferences.observeWakeWordHotkey() } returns flow

        assertSame(flow, ObserveWakeWordHotkeyUseCase(preferences)())

        verify(exactly = 1) { preferences.observeWakeWordHotkey() }
        confirmVerified(preferences, repository)
    }

    @Test
    fun `数字と英字のキーと未割り当てを保存する`() =
        runTest {
            val assigned = WakeWordHotkey(0x41, ManualReadoutHotkeyModifiers.CTRL_SHIFT)
            val unassigned = WakeWordHotkey()
            coEvery { preferences.saveWakeWordHotkey(assigned) } returns Unit
            coEvery { preferences.saveWakeWordHotkey(unassigned) } returns Unit
            val save = SaveWakeWordHotkeyUseCase(preferences)

            save(assigned)
            save(unassigned)

            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(assigned) }
            coVerify(exactly = 1) { preferences.saveWakeWordHotkey(unassigned) }
            confirmVerified(preferences, repository)
        }

    @Test
    fun `数字と英字以外のキーは保存しない`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                SaveWakeWordHotkeyUseCase(preferences)(WakeWordHotkey(0x70))
            }

            confirmVerified(preferences, repository)
        }

    @Test
    fun `押下と登録状態の購読はRepositoryのFlowを返す`() {
        val presses = flowOf(Unit)
        val states = flowOf<HotkeyRegistrationState>(HotkeyRegistrationState.Registered)
        every { repository.pressStream() } returns presses
        every { repository.registrationStateStream() } returns states

        assertSame(presses, ObserveWakeWordHotkeyPressesUseCase(repository)())
        assertSame(states, ObserveWakeWordHotkeyRegistrationStateUseCase(repository)())

        verify(exactly = 1) { repository.pressStream() }
        verify(exactly = 1) { repository.registrationStateStream() }
        confirmVerified(preferences, repository)
    }

    @Test
    fun `再試行をRepositoryに依頼する`() =
        runTest {
            coEvery { repository.retryRegistration() } returns Unit

            RetryWakeWordHotkeyRegistrationUseCase(repository)()

            coVerify(exactly = 1) { repository.retryRegistration() }
            confirmVerified(preferences, repository)
        }
}
