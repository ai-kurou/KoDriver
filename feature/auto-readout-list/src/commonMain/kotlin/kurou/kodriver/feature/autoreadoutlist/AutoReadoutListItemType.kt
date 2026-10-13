package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultReadoutOrder

sealed class AutoReadoutListItemType(
    val id: ReadoutItemKey,
) {
    fun belongsTo(simulator: Simulator): Boolean =
        when (simulator) {
            is Simulator.LmuWindows -> this is LmuWindowsAutoReadoutListItemType
            is Simulator.Gt7Ps5 -> this is Gt7Ps5AutoReadoutListItemType
            is Simulator.AceWindows -> this is AceWindowsAutoReadoutListItemType
        }

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

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = defaultReadoutOrder(simulator)
    }
}
