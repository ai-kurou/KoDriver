package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutStartSoundEnabledPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveReadoutStartSoundEnabledStateUseCaseTest {
    private val repository: ReadoutStartSoundEnabledPreferencesRepository = mockk()

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runTest {
            val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
            every { repository.observeStartSoundEnabledStates() } returns states
            coEvery {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)
            } answers {
                states.update { it + (LmuWindowsReadoutItemKey.Flag.Root to false) }
            }
            coEvery {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
            } answers {
                states.update { it + (LmuWindowsReadoutItemKey.Flag.Root to true) }
            }
            val saveUseCase = SaveReadoutStartSoundEnabledStateUseCase(repository)
            val observeUseCase = ObserveReadoutStartSoundEnabledStatesUseCase(repository)

            saveUseCase(LmuWindowsReadoutItemKey.Flag.Root, false)
            assertEquals(false, observeUseCase().first()[LmuWindowsReadoutItemKey.Flag.Root])

            saveUseCase(LmuWindowsReadoutItemKey.Flag.Root, true)
            assertEquals(true, observeUseCase().first()[LmuWindowsReadoutItemKey.Flag.Root])
            coVerify(exactly = 1) {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, false)
            }
            coVerify(exactly = 1) {
                repository.saveStartSoundEnabledState(LmuWindowsReadoutItemKey.Flag.Root, true)
            }
            verify(exactly = 2) { repository.observeStartSoundEnabledStates() }
            confirmVerified(repository)
        }
}
