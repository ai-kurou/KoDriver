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
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(
    initialHighThreshold: Int = 90,
): LmuWindowsTyreTemperaturePreferencesRepository {
    val repository = mockk<LmuWindowsTyreTemperaturePreferencesRepository>()
    val highThreshold = MutableStateFlow(initialHighThreshold)
    val enabledStates = MutableStateFlow<Map<ReadoutItemKey, Boolean>>(emptyMap())
    every { repository.observeHighThresholdCelsius() } returns highThreshold
    coEvery { repository.saveHighThresholdCelsius(100) } answers { highThreshold.update { 100 } }
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

class ObserveLmuWindowsTyreTemperatureEnabledStatesUseCaseTest {

    @Test
    fun `初期値はOverheatWarningとLowWarningのデフォルトtrueを返す`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)

        val expected = mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
        io.mockk.coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        }
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() = runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository()
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
            ),
            useCase().first(),
        )
        io.mockk.coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)
        }
        io.mockk.verify(exactly = 1) { repo.observeEnabledStates() }
        io.mockk.confirmVerified(repo)
    }
}
