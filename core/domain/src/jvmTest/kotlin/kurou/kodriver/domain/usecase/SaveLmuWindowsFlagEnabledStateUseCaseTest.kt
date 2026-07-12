package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsFlagPreferencesRepository(): LmuWindowsFlagPreferencesRepository {
    val repository = mockk<LmuWindowsFlagPreferencesRepository>()
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeFlagEnabledStates() } returns states
    coEvery { repository.saveFlagEnabledState(any(), any()) } answers {
        states.update { it + (firstArg<ReadoutItemKey>() to secondArg<Boolean>()) }
    }
    return repository
}

class SaveLmuWindowsFlagEnabledStateUseCaseTest {

    @Test
    fun `指定したフラグの有効状態が保存される`() = runBlocking {
        val repository = createLmuWindowsFlagPreferencesRepository()
        val useCase = SaveLmuWindowsFlagEnabledStateUseCase(repository)

        useCase(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.RedFlag to false),
            repository.observeFlagEnabledStates().first(),
        )
    }
}
