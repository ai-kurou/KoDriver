@file:Suppress("FunctionNaming")

package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    repository: LmuWindowsVehicleApproachPreferencesRepository,
): LmuWindowsVehicleApproachPreferencesRepository {
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeEnabledStates() } returns enabledStates
    listOf(
        ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning,
        ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning,
        ReadoutItemKey.LmuWindows.TyreTemperature.Root,
        ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout,
        ReadoutItemKey.LmuWindows.VehicleApproach.Sustained,
        ReadoutItemKey.LmuWindows.VehicleDamage.Overheat,
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
    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はStartReadoutがtrue・Sustainedがfalseのデフォルトを返す`() =
        runBlocking {
            val repo = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true,
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to false,
                )
            assertEquals(expected, useCase().first())
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runBlocking {
            val repo = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCase = ObserveLmuWindowsVehicleApproachEnabledStatesUseCase(repo)

            repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, true)

            val expected =
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout to true,
                    ReadoutItemKey.LmuWindows.VehicleApproach.Sustained to true,
                )
            assertEquals(expected, useCase().first())
            coVerify(exactly = 1) {
                repo.saveEnabledState(ReadoutItemKey.LmuWindows.VehicleApproach.Sustained, true)
            }
            verify(exactly = 1) { repo.observeEnabledStates() }
            confirmVerified(repo)
        }
}
