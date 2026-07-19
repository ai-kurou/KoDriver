@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveQueueEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repository = mockk<QueuePreferencesRepository>()
        val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeQueueEnabledStates() } returns states
        coEvery { repository.saveQueueEnabledState(any(), any()) } answers {
            states.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
        }
        val saveUseCase = SaveQueueEnabledStateUseCase(repository)
        val observeUseCase = ObserveQueueEnabledStatesUseCase(repository)

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, true)
        assertEquals(true, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, false)
        assertEquals(false, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])
    }
}
