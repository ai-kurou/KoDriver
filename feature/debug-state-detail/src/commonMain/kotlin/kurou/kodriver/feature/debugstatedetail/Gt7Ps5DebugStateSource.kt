package kurou.kodriver.feature.debugstatedetail

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.Gt7Ps5VehicleClassData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5VehicleClassUseCase

internal data class Gt7Ps5DebugStateUseCases(
    val observeTelemetry: ObserveGt7Ps5UseCase,
    val observeVehicleClass: ObserveGt7Ps5VehicleClassUseCase,
)

internal class Gt7Ps5DebugStateSource(
    scope: CoroutineScope,
    useCases: Gt7Ps5DebugStateUseCases,
    markCardsReceived: (Simulator, Set<DebugStateCardKey>) -> Unit,
) {
    private val telemetry: StateFlow<Gt7Ps5TelemetryData?> =
        useCases
            .observeTelemetry()
            .onEach {
                markCardsReceived(
                    Simulator.Gt7Ps5,
                    setOf(
                        DebugStateCardKey.CURRENT_LAP,
                        DebugStateCardKey.BEST_LAP,
                        DebugStateCardKey.TYRE_TEMPERATURE,
                        DebugStateCardKey.FUEL_CONSUMPTION,
                    ),
                )
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private val vehicleClass: StateFlow<Gt7Ps5VehicleClassData?> =
        useCases
            .observeVehicleClass()
            .onEach {
                markCardsReceived(Simulator.Gt7Ps5, setOf(DebugStateCardKey.VEHICLE_CLASS))
            }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    val state: StateFlow<Gt7Ps5DebugState> =
        combine(telemetry, vehicleClass) { telemetry, vehicleClass ->
            Gt7Ps5DebugState(telemetry = telemetry, vehicleClass = vehicleClass)
        }.stateIn(scope, SharingStarted.Eagerly, Gt7Ps5DebugState())
}
