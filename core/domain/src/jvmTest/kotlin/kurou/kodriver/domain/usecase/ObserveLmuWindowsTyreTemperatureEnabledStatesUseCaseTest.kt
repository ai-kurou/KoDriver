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
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsTyreTemperaturePreferencesRepository(
    repository: LmuWindowsTyreTemperaturePreferencesRepository,
    initialHighThreshold: Int = 90,
): LmuWindowsTyreTemperaturePreferencesRepository {
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

    @MockK
    private lateinit var repository: LmuWindowsTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値はOverheatWarningとLowWarningのデフォルトtrueを返す`() =
        runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository(repository)
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        val expected =
            mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
        verify(exactly = 1) { repo.observeEnabledStates() }
        confirmVerified(repo)
    }

    @Test
    fun `保存済みの値はデフォルトより優先される`() =
        runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository(repository)
        val useCase = ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(repo)

        repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)

        val expected =
            mapOf<ReadoutItemKey, Boolean>(
            ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false,
            ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
        )
        assertEquals(expected, useCase().first())
        coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)
        }
        verify(exactly = 1) { repo.observeEnabledStates() }
        confirmVerified(repo)
    }

    @Test
    fun `デフォルトにないキーを保存した場合そのエントリも返す`() =
        runBlocking {
        val repo = createLmuWindowsTyreTemperaturePreferencesRepository(repository)
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
        coVerify(exactly = 1) {
            repo.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)
        }
        verify(exactly = 1) { repo.observeEnabledStates() }
        confirmVerified(repo)
    }
}
