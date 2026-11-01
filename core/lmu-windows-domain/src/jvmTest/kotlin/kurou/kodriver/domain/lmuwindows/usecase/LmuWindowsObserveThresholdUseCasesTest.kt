package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingFuelPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRemainingVirtualEnergyPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsTyreWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassBrakeWearPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsObserveThresholdUseCasesTest {
    @Test
    fun `閾値のObserve系UseCaseはリポジトリの値をそのまま返す`() {
        assertObserves<LmuWindowsTyreWearPreferencesRepository, _>(
            "ObserveLmuWindowsTyreWearThresholdPercentageUseCase",
            50,
            observe = { observeThresholdPercentage() },
            useCase = { ObserveLmuWindowsTyreWearThresholdPercentageUseCase(it)() },
        )
        assertObserves<LmuWindowsRemainingVirtualEnergyPreferencesRepository, _>(
            "ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase",
            20,
            observe = { observeThresholdPercentage() },
            useCase = { ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase(it)() },
        )
        assertObserves<LmuWindowsRemainingFuelPreferencesRepository, _>(
            "ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase",
            20,
            observe = { observeThresholdPercentage() },
            useCase = { ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase(it)() },
        )
        assertObserves<LmuWindowsVehicleClassBrakeWearPreferencesRepository, _>(
            "ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase",
            mapOf(LmuWindowsVehicleClassData.Gte to 35),
            observe = { observeLowThresholdPercent() },
            useCase = { ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase(it)() },
        )
        assertObserves<LmuWindowsVehicleClassBrakeTemperaturePreferencesRepository, _>(
            "ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase",
            mapOf(LmuWindowsVehicleClassData.Gte to 700),
            observe = { observeHighThresholdCelsius() },
            useCase = { ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase(it)() },
        )
        assertObserves<LmuWindowsVehicleClassTyreTemperaturePreferencesRepository, _>(
            "ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase",
            mapOf(LmuWindowsVehicleClassData.Gte to Celsius(90)),
            observe = { observeHighThresholdCelsius() },
            useCase = { ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(it)() },
        )
    }

    private inline fun <reified R : Any, T> assertObserves(
        useCaseName: String,
        expected: T,
        crossinline observe: R.() -> Flow<T>,
        crossinline useCase: (R) -> Flow<T>,
    ) = runTest {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)
        try {
            assertEquals(expected, useCase(repository).first())
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
