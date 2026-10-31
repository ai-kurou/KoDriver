package kurou.kodriver.feature.gt7ps5narrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

/** GT7 の購読切り替えと合成を担う cold Flow。共有・初期値・購読の寿命は ViewModel が管理する。 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class Gt7Ps5NarratorFlow(
    private val selectedSimulator: Flow<Simulator>,
) {
    // listPane と detailPane の設定を統合し、同じキーでは detailPane を優先する。
    fun enabledStates(
        observeAutoReadoutEnabledStates: (String) -> Flow<Map<ReadoutItemKey, Boolean>>,
        tyreTemperatureStates: Flow<Map<ReadoutItemKey, Boolean>>,
    ): Flow<Map<ReadoutItemKey, Boolean>> =
        combine(
            selectedSimulator.flatMapLatest { simulator -> observeAutoReadoutEnabledStates(simulator.id) },
            tyreTemperatureStates,
        ) { readoutStates, tyreStates ->
            readoutStates + tyreStates
        }

    fun autoReadoutOrder(
        observeAutoReadoutOrder: (Simulator) -> Flow<List<ReadoutItemKey>>,
    ): Flow<List<ReadoutItemKey>> = selectedSimulator.flatMapLatest { simulator -> observeAutoReadoutOrder(simulator) }

    fun telemetry(observeGt7Ps5: () -> Flow<Gt7Ps5TelemetryData>): Flow<Gt7Ps5TelemetryData> =
        selectedSimulator.flatMapLatest { simulator ->
            if (simulator !is Simulator.Gt7Ps5) {
                emptyFlow()
            } else {
                observeGt7Ps5()
            }
        }
}
