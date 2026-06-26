package kurou.kodriver.core.gt7ps5data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Gt7Ps5RemainingFuelLapsPreferencesRepositoryImplTest {

    private val fakeReadoutPreferences = FakeReadoutPreferencesRepository()
    private val repository = Gt7Ps5RemainingFuelLapsPreferencesRepositoryImpl(fakeReadoutPreferences)

    @Test
    fun `初期値は true`() = runTest {
        assertTrue(repository.observeEnabled().first())
    }

    @Test
    fun `保存した有効状態を取得できる`() = runTest {
        repository.saveEnabled(false)

        assertFalse(repository.observeEnabled().first())
    }

    @Test
    fun `有効状態を上書き保存できる`() = runTest {
        repository.saveEnabled(false)
        repository.saveEnabled(true)

        assertTrue(repository.observeEnabled().first())
    }
}

private class FakeReadoutPreferencesRepository : ReadoutPreferencesRepository {
    private val states = MutableStateFlow<Map<String, Map<ReadoutItemKey, Boolean>>>(emptyMap())

    override fun observeReadoutEnabledStates(simulator: String): Flow<Map<ReadoutItemKey, Boolean>> =
        states.map { it[simulator] ?: emptyMap() }

    override suspend fun saveReadoutEnabledState(simulator: String, key: ReadoutItemKey, enabled: Boolean) {
        states.update { current ->
            val simulatorStates = current[simulator]?.toMutableMap() ?: mutableMapOf()
            simulatorStates[key] = enabled
            current + (simulator to simulatorStates)
        }
    }

    override fun observeReadoutOrder(simulator: String): Flow<List<ReadoutItemKey>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun saveReadoutOrder(simulator: String, order: List<ReadoutItemKey>) = Unit
}
