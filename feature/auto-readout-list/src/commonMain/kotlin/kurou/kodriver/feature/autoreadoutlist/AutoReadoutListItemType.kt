package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultAutoReadoutOrder

public sealed class AutoReadoutListItemType(
    public val id: ReadoutItemKey,
    private val simulator: Simulator,
) {
    internal fun belongsTo(simulator: Simulator): Boolean = this.simulator == simulator

    public companion object {
        public fun fromId(
            simulator: Simulator,
            id: ReadoutItemKey,
        ): AutoReadoutListItemType? =
            when (simulator) {
                is Simulator.LmuWindows -> LmuWindowsAutoReadoutListItemType.fromId(id)
                is Simulator.Gt7Ps5 -> Gt7Ps5AutoReadoutListItemType.fromId(id)
                is Simulator.AceWindows -> AceWindowsAutoReadoutListItemType.fromId(id)
            }

        public fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = defaultAutoReadoutOrder(simulator)
    }
}
