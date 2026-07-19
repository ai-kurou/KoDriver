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

private fun createQueuePreferencesRepository(): QueuePreferencesRepository {
    val repository = mockk<QueuePreferencesRepository>()
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeQueueEnabledStates() } returns enabledStates
    coEvery { repository.saveQueueEnabledState(any(), any()) } answers {
        enabledStates.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class SaveQueueEnabledStateUseCaseTest {

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() = runBlocking {
        val repo = createQueuePreferencesRepository()
        val saveUseCase = SaveQueueEnabledStateUseCase(repo)
        val observeUseCase = ObserveQueueEnabledStatesUseCase(repo)

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, true)
        assertEquals(true, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, false)
        assertEquals(false, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])
    }
}
