@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleDamageEnabledStatesUseCaseTest {

    @Test
    fun `初期値はOverheatのデフォルトtrueを返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        assertEquals(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to true), useCase().first())
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.Overheat, false)

        assertEquals(mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to false), useCase().first())
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.VehicleDamage, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Overheat to true, ReadoutItemKey.VehicleDamage to false),
            useCase().first(),
        )
    }
}

internal class FakeLmuWindowsVehicleDamagePreferencesRepository : LmuWindowsVehicleDamagePreferencesRepository {
    private val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())

    override fun observeEnabledStates(): Flow<Map<ReadoutItemKey, Boolean>> = states

    override suspend fun saveEnabledState(key: ReadoutItemKey, enabled: Boolean) {
        states.update { it + (key to enabled) }
    }
}
