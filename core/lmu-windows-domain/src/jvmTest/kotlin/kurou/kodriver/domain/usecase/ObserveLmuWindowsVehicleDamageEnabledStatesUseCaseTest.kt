package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleDamagePreferencesRepository(
    repository: LmuWindowsVehicleDamagePreferencesRepository,
): LmuWindowsVehicleDamagePreferencesRepository {
    val states = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns states
    listOf(
        LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
        LmuWindowsReadoutItemKey.VehicleDamage.PartDetached,
        LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached,
        LmuWindowsReadoutItemKey.VehicleDamage.Root,
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
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk()

    @Test
    fun `初期値はOverheatとPartDetachedとTyreDetachedのデフォルトtrueを返す`() =
        runTest {
            val repo = createLmuWindowsVehicleDamagePreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to true,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createLmuWindowsVehicleDamagePreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

            repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to true,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runTest {
            val repo = createLmuWindowsVehicleDamagePreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleDamageEnabledStatesUseCase(repo)

            repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.PartDetached to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                ),
                useCase().first(),
            )
            coVerify(exactly = 1) {
                repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
