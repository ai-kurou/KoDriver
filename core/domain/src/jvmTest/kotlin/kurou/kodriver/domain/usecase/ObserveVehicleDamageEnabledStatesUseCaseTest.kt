@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.VehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveVehicleDamageEnabledStatesUseCaseTest {

    @Test
    fun `初期値は空Mapを返す`() = runBlocking {
        val repo = FakeVehicleDamagePreferencesRepository()
        val useCase = ObserveVehicleDamageEnabledStatesUseCase(repo)

        assertTrue(useCase().first().isEmpty())
    }

    @Test
    fun `保存済みの値を返す`() = runBlocking {
        val repo = FakeVehicleDamagePreferencesRepository()
        val useCase = ObserveVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState("overheat", true)

        assertEquals(mapOf("overheat" to true), useCase().first())
    }

    @Test
    fun `複数キーを保存した場合すべてのエントリを返す`() = runBlocking {
        val repo = FakeVehicleDamagePreferencesRepository()
        val useCase = ObserveVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState("overheat", true)
        repo.saveEnabledState("part_detached", false)

        assertEquals(mapOf("overheat" to true, "part_detached" to false), useCase().first())
    }
}

internal class FakeVehicleDamagePreferencesRepository : VehicleDamagePreferencesRepository {
    private val states = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    override fun observeEnabledStates(): Flow<Map<String, Boolean>> = states

    override suspend fun saveEnabledState(key: String, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
