package kurou.kodriver.feature.autoreadoutlist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

private data class SimulatorReadoutState(
    val simulator: Simulator,
    val persistedOrder: List<ReadoutItemKey>,
    val autoReadoutEnabledStates: Map<ReadoutItemKey, Boolean>,
)

/** AutoReadoutList の設定を観測し、表示状態を合成する冷たい Flow。 */
class AutoReadoutListFlow(
    observeSelectedSimulator: Flow<Simulator>,
    private val autoReadoutOrderUseCases: AutoReadoutOrderUseCases,
    autoReadoutEnabledUseCases: AutoReadoutEnabledUseCases,
    queueUseCases: QueueUseCases,
    startSoundUseCases: StartSoundUseCases,
    localOrder: Flow<Pair<Simulator?, List<ReadoutItemKey>>>,
    selectedItem: Flow<AutoReadoutListItemType?>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val simulatorReadoutState =
        observeSelectedSimulator.flatMapLatest { simulator ->
            combine(
                autoReadoutOrderUseCases.observeAutoReadoutOrder(simulator.id),
                autoReadoutEnabledUseCases.observeAutoReadoutEnabledStates(simulator.id),
            ) { persistedOrder, enabledStates ->
                SimulatorReadoutState(simulator, persistedOrder, enabledStates)
            }
        }

    private val queueEnabledStates = queueUseCases.observeQueueEnabledStates()
    private val startSoundEnabledStates = startSoundUseCases.observeReadoutStartSoundEnabledStates()
    private val settings =
        combine(queueEnabledStates, startSoundEnabledStates) { queue, sound ->
            queue to sound
        }

    val uiState: Flow<AutoReadoutListUiState> =
        combine(
            simulatorReadoutState,
            localOrder,
            settings,
            selectedItem,
        ) { simulatorState, local, settings, selected ->
            val simulator = simulatorState.simulator
            val items =
                if (local.first == simulator) {
                    local.second
                } else {
                    autoReadoutOrderUseCases.resolveAutoReadoutOrder(
                        persistedOrder = simulatorState.persistedOrder,
                        defaultOrder = AutoReadoutListItemType.defaultOrder(simulator),
                    )
                }
            AutoReadoutListUiState(
                selectedSimulator = simulator,
                items = items,
                autoReadoutEnabledStates = simulatorState.autoReadoutEnabledStates,
                queueEnabledStates = settings.first,
                startSoundEnabledStates = settings.second,
                selectedItem = selected?.takeIf { it.belongsTo(simulator) },
            )
        }
}
