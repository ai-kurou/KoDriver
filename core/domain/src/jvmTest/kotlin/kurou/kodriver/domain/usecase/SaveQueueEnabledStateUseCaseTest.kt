@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.QueuePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveQueueEnabledStateUseCaseTest {

    @MockK
    private lateinit var repository: QueuePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `保存するとFlowに値が反映され・上書きで更新される`() =
        runBlocking {
        val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
        every { repository.observeQueueEnabledStates() } returns states
        coEvery { repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true) } answers {
            states.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to true) }
        }
        coEvery { repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false) } answers {
            states.update { it + (ReadoutItemKey.LmuWindows.Flag.Root to false) }
        }
        val saveUseCase = SaveQueueEnabledStateUseCase(repository)
        val observeUseCase = ObserveQueueEnabledStatesUseCase(repository)

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, true)
        assertEquals(true, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])

        saveUseCase(ReadoutItemKey.LmuWindows.Flag.Root, false)
        assertEquals(false, observeUseCase().first()[ReadoutItemKey.LmuWindows.Flag.Root])
        coVerify(exactly = 1) {
            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, true)
        }
        coVerify(exactly = 1) {
            repository.saveQueueEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
        }
        verify(exactly = 2) { repository.observeQueueEnabledStates() }
        confirmVerified(repository)
    }
}
