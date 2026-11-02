package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultAutoReadoutOrder

sealed class AutoReadoutListItemType(
    val id: ReadoutItemKey,
    private val simulator: Simulator,
) {
    fun belongsTo(simulator: Simulator): Boolean = this.simulator == simulator

    companion object {
        fun fromId(
            simulator: Simulator,
            id: ReadoutItemKey,
        ): AutoReadoutListItemType? =
            when (simulator) {
                is Simulator.LmuWindows -> LmuWindowsAutoReadoutListItemType.fromId(id)
                is Simulator.Gt7Ps5 -> Gt7Ps5AutoReadoutListItemType.fromId(id)
                is Simulator.AceWindows -> AceWindowsAutoReadoutListItemType.fromId(id)
            }

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = defaultAutoReadoutOrder(simulator)
    }
}
