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
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeQueueEnabledStates() } returns states
    coEvery { repository.saveQueueEnabledState(any(), any()) } answers {
        states.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class SaveQueueEnabledStateUseCaseTest {

    @Test
    fun `指定した項目のキュー有効状態が保存される`() = runBlocking {
        val repository = createQueuePreferencesRepository()
        val useCase = SaveQueueEnabledStateUseCase(repository)

        useCase(ReadoutItemKey.LmuWindows.Flag.Root, true)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to true),
            repository.observeQueueEnabledStates().first(),
        )
    }
}
