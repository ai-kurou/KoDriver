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
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    repository: LmuWindowsVehicleApproachPreferencesRepository,
): LmuWindowsVehicleApproachPreferencesRepository {
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
        LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
        LmuWindowsReadoutItemKey.TyreTemperature.Root,
        LmuWindowsReadoutItemKey.VehicleApproach.StartReadout,
        LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
        LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
    ).forEach { key ->
        listOf(true, false).forEach { enabled ->
            coEvery { repository.saveEnabledState(key, enabled) } answers {
                enabledStates.update { it + (key to enabled) }
            }
        }
    }
    return repository
}

class ObserveLmuWindowsVehicleApproachEnabledStatesUseCaseTest {
    private val repository: LmuWindowsVehicleApproachPreferencesRepository = mockk()

    @Test
    fun `初期値はStartReadoutがtrue・Sustainedがfalseのデフォルトを返す`() =
        runTest {
            val repo = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runTest {
            val repo = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

            repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, true)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.StartReadout to true,
                    LmuWindowsReadoutItemKey.VehicleApproach.Sustained to true,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, true)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
