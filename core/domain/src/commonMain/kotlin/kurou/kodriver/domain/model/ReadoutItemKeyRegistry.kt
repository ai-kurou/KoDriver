package kurou.kodriver.domain.model

import kotlin.concurrent.Volatile

/**
 * 手動読み上げに対応するシミュレーターの項目定義と表示上の挙動。
 *
 * [showSimHubGuideLink] は手動読み上げのホットキー案内に SimHub の導入リンクを出すかどうか。
 */
class ManualReadoutSimulatorSpec(
    val itemDefinitions: Map<String, ManualReadoutItemDefinition>,
    val showSimHubGuideLink: Boolean = true,
)

/** A simulator's complete readout key catalog and defaults. [manualReadout] は手動読み上げ非対応なら null。 */
class SimulatorReadoutSpec(
    val simulator: Simulator,
    val keys: List<ReadoutItemKey>,
    val autoReadoutEnabledStateDefault: Map<ReadoutItemKey, Boolean>,
    val startSoundEnabledStateDefault: Map<ReadoutItemKey, Boolean>,
    val queueEnabledStateDefault: Map<ReadoutItemKey, Boolean>,
    val defaultAutoReadoutOrder: List<ReadoutItemKey>,
    val manualReadout: ManualReadoutSimulatorSpec? = null,
)

/** Thread-safe registry; updates replace an immutable snapshot. */
object ReadoutItemKeyRegistry {
    private val registryState = ReadoutItemKeyRegistryState()

    fun register(spec: SimulatorReadoutSpec) = registryState.register(spec)

    fun specOf(simulator: Simulator): SimulatorReadoutSpec = registryState.specOf(simulator)

    /** 登録済みの spec を [Simulator.entries] の順で返す。未登録のシミュレーターは含まれない。 */
    val registeredSpecs: List<SimulatorReadoutSpec> get() = registryState.registeredSpecs

    val entries: List<ReadoutItemKey> get() = registryState.entries

    /** 手動読み上げに対応する登録済みシミュレーターの ID と spec を [Simulator.entries] の順で返す。 */
    val manualReadoutSpecs: List<Pair<String, ManualReadoutSimulatorSpec>>
        get() = registeredSpecs.mapNotNull { spec -> spec.manualReadout?.let { spec.simulator.id to it } }

    /** [simulatorId] の手動読み上げ spec。未登録・非対応なら null。 */
    fun manualReadoutSpecOf(simulatorId: String): ManualReadoutSimulatorSpec? =
        manualReadoutSpecs.firstOrNull { it.first == simulatorId }?.second

    /** [simulatorId] の手動読み上げ画面で SimHub の導入リンクを出すか。未登録・非対応は既定の true。 */
    fun showsManualReadoutSimHubGuideLink(simulatorId: String): Boolean =
        manualReadoutSpecOf(simulatorId)?.showSimHubGuideLink ?: true

    fun fromValue(value: String): ReadoutItemKey? = registryState.fromValue(value)

    fun topLevelEntriesOf(simulator: Simulator): List<ReadoutItemKey.TopLevel> =
        registryState.topLevelEntriesOf(simulator)

    fun autoReadoutEnabledDefault(key: ReadoutItemKey): Boolean? = registryState.autoReadoutEnabledDefault(key)
}

internal class ReadoutItemKeyRegistryState {
    @Volatile
    private var specs: Map<Simulator, SimulatorReadoutSpec> = emptyMap()

    fun register(spec: SimulatorReadoutSpec) {
        synchronized(this) {
            specs = specs + (spec.simulator to spec.copySnapshot())
        }
    }

    fun specOf(simulator: Simulator): SimulatorReadoutSpec {
        val snapshot = specs
        return snapshot[simulator] ?: error("ReadoutItemKeyRegistry: ${simulator.id} is not registered")
    }

    val registeredSpecs: List<SimulatorReadoutSpec>
        get() {
            val snapshot = specs
            return Simulator.entries.mapNotNull { snapshot[it] }
        }

    val entries: List<ReadoutItemKey>
        get() {
            val snapshot = specs
            return Simulator.entries.flatMap { simulator -> snapshot[simulator]?.keys.orEmpty() }
        }

    fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }

    fun topLevelEntriesOf(simulator: Simulator): List<ReadoutItemKey.TopLevel> =
        specOf(simulator).keys.filterIsInstance<ReadoutItemKey.TopLevel>()

    fun autoReadoutEnabledDefault(key: ReadoutItemKey): Boolean? {
        val snapshot = specs
        return snapshot.values.firstNotNullOfOrNull { spec -> spec.autoReadoutEnabledStateDefault[key] }
    }

    private fun SimulatorReadoutSpec.copySnapshot() =
        SimulatorReadoutSpec(
            simulator = simulator,
            keys = keys.toList(),
            autoReadoutEnabledStateDefault = autoReadoutEnabledStateDefault.toMap(),
            startSoundEnabledStateDefault = startSoundEnabledStateDefault.toMap(),
            queueEnabledStateDefault = queueEnabledStateDefault.toMap(),
            defaultAutoReadoutOrder = defaultAutoReadoutOrder.toList(),
            manualReadout =
                manualReadout?.let {
                    ManualReadoutSimulatorSpec(it.itemDefinitions.toMap(), it.showSimHubGuideLink)
                },
        )
}
