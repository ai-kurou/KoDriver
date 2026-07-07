package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLmuWindowsFlagEnabledStateUseCaseTest {

    @Test
    fun `指定したフラグの有効状態が保存される`() = runBlocking {
        val repository = FakeLmuWindowsFlagPreferencesRepository()
        val useCase = SaveLmuWindowsFlagEnabledStateUseCase(repository)

        useCase(ReadoutItemKey.LmuWindows.Flag.RedFlag, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.RedFlag to false),
            repository.observeFlagEnabledStates().first(),
        )
    }
}
