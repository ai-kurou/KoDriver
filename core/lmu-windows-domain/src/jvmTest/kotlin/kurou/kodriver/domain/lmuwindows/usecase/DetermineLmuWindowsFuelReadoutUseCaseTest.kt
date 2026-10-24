package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.engine.LmuWindowsRemainingFuelWarning
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

class DetermineLmuWindowsFuelReadoutUseCaseTest {
    private val useCase = DetermineLmuWindowsFuelReadoutUseCase()

    @Test
    fun `閾値以下になった瞬間に一度だけ警告する`() {
        val warned =
            useCase.determineRemainingFuel(LmuWindowsNarratorState(), LmuWindowsRemainingFuelData(29), settings())
        val repeated = useCase.determineRemainingFuel(warned.state, LmuWindowsRemainingFuelData(20), settings())

        assertEquals(listOf(LmuWindowsRemainingFuelWarning(THRESHOLD)), warned.events)
        assertEquals(true, warned.state.remainingFuelWarned)
        assertEquals(emptyList(), repeated.events)
    }

    @Test
    fun `閾値を上回ると再武装して再度警告できる`() {
        val warned =
            useCase.determineRemainingFuel(LmuWindowsNarratorState(), LmuWindowsRemainingFuelData(20), settings())
        val recovered = useCase.determineRemainingFuel(warned.state, LmuWindowsRemainingFuelData(31), settings())
        val warnedAgain = useCase.determineRemainingFuel(recovered.state, LmuWindowsRemainingFuelData(20), settings())

        assertEquals(false, recovered.state.remainingFuelWarned)
        assertEquals(listOf(LmuWindowsRemainingFuelWarning(THRESHOLD)), warnedAgain.events)
    }

    @Test
    fun `閾値より多ければ警告しない`() {
        val decision =
            useCase.determineRemainingFuel(LmuWindowsNarratorState(), LmuWindowsRemainingFuelData(31), settings())

        assertEquals(emptyList(), decision.events)
        assertEquals(false, decision.state.remainingFuelWarned)
    }

    @Test
    fun `Rootが無効なら警告しない`() {
        val decision =
            useCase.determineRemainingFuel(
                LmuWindowsNarratorState(),
                LmuWindowsRemainingFuelData(20),
                settings(rootEnabled = false),
            )

        assertEquals(emptyList(), decision.events)
    }

    @Test
    fun `WarningReadoutが無効なら警告しない`() {
        val decision =
            useCase.determineRemainingFuel(
                LmuWindowsNarratorState(),
                LmuWindowsRemainingFuelData(20),
                settings(warningReadoutEnabled = false),
            )

        assertEquals(emptyList(), decision.events)
    }

    @Test
    fun `エネルギー源がVirtualEnergyなら警告せず警告済み状態を維持する`() {
        val state = LmuWindowsNarratorState(remainingFuelWarned = true)

        val decision =
            useCase.determineRemainingFuel(
                state,
                LmuWindowsRemainingFuelData(1),
                settings(energySource = LmuWindowsEnergySource.VirtualEnergy),
            )

        assertEquals(emptyList(), decision.events)
        assertEquals(state, decision.state)
    }

    private fun settings(
        rootEnabled: Boolean = true,
        warningReadoutEnabled: Boolean = true,
        energySource: LmuWindowsEnergySource = LmuWindowsEnergySource.Fuel,
    ): LmuWindowsNarratorReadoutSettings {
        val enabledStates: Map<ReadoutItemKey, Boolean> =
            mapOf(
                LmuWindowsReadoutItemKey.RemainingFuel.Root to rootEnabled,
                LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout to warningReadoutEnabled,
            )
        return LmuWindowsNarratorReadoutSettings(
            enabledStates = enabledStates,
            currentLap = 1,
            skipFirstLap = false,
            vehicleApproachSustainedApproachDurationSeconds = 7,
            tyreTemperatureHighThresholdCelsius = Celsius(90),
            tyreTemperatureLowWarningPhases = setOf(SessionPhase.GARAGE),
            tyreWearThresholdPercentage = 30,
            brakeTemperatureHighThresholdCelsius = Celsius(700),
            brakeWearLowThresholdPercent = 20,
            remainingVirtualEnergyThresholdPercentage = 30,
            remainingFuelThresholdPercentage = THRESHOLD,
            energySource = energySource,
            pitTimingVirtualEnergyLapsThreshold = 3,
            pitTimingTyreWearLapsThreshold = 3,
        )
    }

    private companion object {
        const val THRESHOLD = 30
    }
}
