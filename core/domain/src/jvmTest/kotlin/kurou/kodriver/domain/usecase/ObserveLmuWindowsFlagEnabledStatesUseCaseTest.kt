@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsFlagEnabledStatesUseCaseTest {

    @Test
    fun `初期値はフラグ4種のデフォルトtrueを返す`() = runBlocking {
        val repo = FakeLmuWindowsFlagPreferencesRepository()
        val useCase = ObserveLmuWindowsFlagEnabledStatesUseCase(repo)

        assertEquals(
            mapOf(
                ReadoutItemKey.BlueFlag to true,
                ReadoutItemKey.SectorYellowFlag to true,
                ReadoutItemKey.FullCourseYellow to true,
                ReadoutItemKey.RedFlag to true,
            ),
            useCase().first(),
        )
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = FakeLmuWindowsFlagPreferencesRepository()
        val useCase = ObserveLmuWindowsFlagEnabledStatesUseCase(repo)

        repo.saveFlagEnabledState(ReadoutItemKey.RedFlag, false)

        assertEquals(
            mapOf(
                ReadoutItemKey.BlueFlag to true,
                ReadoutItemKey.SectorYellowFlag to true,
                ReadoutItemKey.FullCourseYellow to true,
                ReadoutItemKey.RedFlag to false,
            ),
            useCase().first(),
        )
    }
}

private class FakeLmuWindowsFlagPreferencesRepository : LmuWindowsFlagPreferencesRepository {
    private val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeFlagEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveFlagEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
