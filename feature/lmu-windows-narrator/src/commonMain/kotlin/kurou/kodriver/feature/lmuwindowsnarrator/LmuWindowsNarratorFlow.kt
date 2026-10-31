package kurou.kodriver.feature.lmuwindowsnarrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsRemainingFuelData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreWearData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassEnergySource
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent
import kurou.kodriver.domain.lmuwindows.model.resolveLmuWindowsVehicleClassTyreTemperatureHighThresholdCelsius
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

/** LMU 固有の cold Flow の合成。共有・初期値・受信時刻・イベント処理は ViewModel が管理する。 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class LmuWindowsNarratorFlow(
    private val selectedSimulator: Flow<Simulator>,
) {
    // ソースの生成も選択後に行い、選択解除で購読をキャンセルする。
    fun <T> telemetry(observe: () -> Flow<T>): Flow<T> =
        selectedSimulator.flatMapLatest { simulator ->
            if (simulator !is Simulator.LmuWindows) emptyFlow() else observe()
        }

    @Suppress("LongParameterList")
    fun mergedEnabledStates(
        observeAutoReadoutEnabledStates: (String) -> Flow<Map<ReadoutItemKey, Boolean>>,
        flagStates: Flow<Map<ReadoutItemKey, Boolean>>,
        vehicleDamageStates: Flow<Map<ReadoutItemKey, Boolean>>,
        tyreTemperatureStates: Flow<Map<ReadoutItemKey, Boolean>>,
        vehicleApproachStates: Flow<Map<ReadoutItemKey, Boolean>>,
        pitTimingStates: Flow<Map<ReadoutItemKey, Boolean>>,
    ): Flow<Map<ReadoutItemKey, Boolean>> =
        combine(
            listOf(
                selectedSimulator.flatMapLatest { observeAutoReadoutEnabledStates(it.id) },
                flagStates,
                vehicleDamageStates,
                tyreTemperatureStates,
                vehicleApproachStates,
                pitTimingStates,
            ),
        ) { states: Array<Map<ReadoutItemKey, Boolean>> ->
            states.fold(emptyMap<ReadoutItemKey, Boolean>()) { acc, enabledStates -> acc + enabledStates }
        }

    fun autoReadoutOrder(observe: (Simulator) -> Flow<List<ReadoutItemKey>>): Flow<List<ReadoutItemKey>> =
        selectedSimulator.flatMapLatest { observe(it) }

    fun currentLap(telemetry: Flow<LmuWindowsTelemetryData>): Flow<Int> = telemetry.map { it.timing.currentLap }

    fun energySource(vehicleClass: Flow<LmuWindowsVehicleClassData>): Flow<LmuWindowsEnergySource> =
        vehicleClass.map(::lmuWindowsVehicleClassEnergySource)

    fun tyreHighThreshold(
        vehicleClass: Flow<LmuWindowsVehicleClassData>,
        thresholds: Flow<Map<LmuWindowsVehicleClassData, Celsius>>,
    ): Flow<Celsius> =
        combine(vehicleClass, thresholds) { currentClass, values ->
            resolveLmuWindowsVehicleClassTyreTemperatureHighThresholdCelsius(values, currentClass)
        }

    fun brakeHighThreshold(
        vehicleClass: Flow<LmuWindowsVehicleClassData>,
        thresholds: Flow<Map<LmuWindowsVehicleClassData, Int>>,
    ): Flow<Celsius> =
        combine(vehicleClass, thresholds) { currentClass, values ->
            Celsius(resolveLmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsius(values, currentClass))
        }

    fun brakeLowThreshold(
        vehicleClass: Flow<LmuWindowsVehicleClassData>,
        thresholds: Flow<Map<LmuWindowsVehicleClassData, Int>>,
    ): Flow<Int> =
        combine(vehicleClass, thresholds) { currentClass, values ->
            resolveLmuWindowsVehicleClassBrakeWearLowThresholdPercent(values, currentClass)
        }

    fun tyreTemperature(
        temperature: Flow<LmuWindowsTyreCarcassTemperatureData>,
        raceFlags: Flow<LmuWindowsRaceFlagsData>,
    ): Flow<Pair<LmuWindowsTyreCarcassTemperatureData, LmuWindowsRaceFlagsData>> =
        combine(temperature, raceFlags) { currentTemperature, flags -> currentTemperature to flags }

    fun pitTiming(
        telemetry: Flow<LmuWindowsTelemetryData>,
        virtualEnergy: Flow<LmuWindowsVirtualEnergyData>,
        tyreWear: Flow<LmuWindowsTyreWearData>,
        fuel: Flow<LmuWindowsRemainingFuelData>,
    ): Flow<LmuWindowsPitTimingSnapshot> =
        combine(
            telemetry,
            virtualEnergy.map<LmuWindowsVirtualEnergyData, LmuWindowsVirtualEnergyData?> { it }.onStart { emit(null) },
            tyreWear.map<LmuWindowsTyreWearData, LmuWindowsTyreWearData?> { it }.onStart { emit(null) },
            fuel.map<LmuWindowsRemainingFuelData, LmuWindowsRemainingFuelData?> { it }.onStart { emit(null) },
        ) { currentTelemetry, energy, wear, remainingFuel ->
            LmuWindowsPitTimingSnapshot(currentTelemetry, energy, wear, remainingFuel)
        }
}
