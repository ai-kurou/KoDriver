package kurou.kodriver.feature.acewindowsnarrator

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

/** ACE の購読切替と設定・要求の合成を担う cold Flow。共有・初期値・イベント処理は ViewModel が管理する。 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AceWindowsNarratorFlow(
    private val selectedSimulator: Flow<Simulator>,
) {
    fun listEnabledStates(
        observeEnabledStates: (String) -> Flow<Map<ReadoutItemKey, Boolean>>,
    ): Flow<Map<ReadoutItemKey, Boolean>> =
        selectedSimulator.flatMapLatest { simulator -> observeEnabledStates(simulator.id) }

    fun autoReadoutOrder(observeOrder: (Simulator) -> Flow<List<ReadoutItemKey>>): Flow<List<ReadoutItemKey>> =
        selectedSimulator.flatMapLatest { simulator -> observeOrder(simulator) }

    // 一覧と詳細のスイッチを統合する。同じキーは後ろの設定を優先する。
    fun mergedEnabledStates(
        listEnabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
        flagEnabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
        tyreTemperatureEnabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
        vehicleApproachEnabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    ): Flow<Map<ReadoutItemKey, Boolean>> =
        combine(
            listEnabledStates,
            flagEnabledStates,
            tyreTemperatureEnabledStates,
            vehicleApproachEnabledStates,
        ) { readoutStates, flagStates, tyreTemperatureStates, vehicleApproachStates ->
            readoutStates + flagStates + tyreTemperatureStates + vehicleApproachStates
        }

    /** ACE 非選択時は値を補完せず、購読中のテレメトリをキャンセルする。 */
    fun <T> telemetry(observeTelemetry: () -> Flow<T>): Flow<T> =
        selectedSimulator.flatMapLatest { simulator ->
            if (simulator !is Simulator.AceWindows) emptyFlow() else observeTelemetry()
        }

    fun manualReadoutRequests(
        requests: Flow<ManualReadoutRequest>,
        hotkeyPresses: Flow<ManualReadoutRequest>,
    ): Flow<ManualReadoutRequest> = merge(requests, hotkeyPresses)
}
