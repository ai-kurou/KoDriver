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
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleDamagePreferencesRepository(): LmuWindowsVehicleDamagePreferencesRepository {
    val repository = mockk<LmuWindowsVehicleDamagePreferencesRepository>()
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns states
    listOf(
        ReadoutItemKey.LmuWindows.VehicleDamage.Overheat,
        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                states.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsVehicleDamageEnabledStatesUseCaseTest {

    @Test
    fun `初期値はOverheatのデフォルトtrueを返す`() = runBlocking {
        val repo = createLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        val expected = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true)
        assertEquals(expected, useCase().first())
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = createLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)

        val expected = mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to false)
        assertEquals(expected, useCase().first())
        io.mockk.coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Overheat, false)
        }
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() = runBlocking {
        val repo = createLmuWindowsVehicleDamagePreferencesRepository()
        val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.VehicleDamage.Overheat to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
            ),
            useCase().first(),
        )
        io.mockk.coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleDamage.Root, false)
        }
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }
}
