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

        val expected = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true)
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)

        val expected = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false)
        assertEquals(expected, useCase().first())
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() = runBlocking {
        val repo = FakeLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
            ),
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
