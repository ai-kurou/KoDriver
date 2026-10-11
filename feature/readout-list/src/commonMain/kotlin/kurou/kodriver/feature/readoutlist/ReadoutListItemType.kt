package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultReadoutOrder

sealed class ReadoutListItemType(
    val id: ReadoutItemKey,
) {
    fun belongsTo(simulator: Simulator): Boolean =
        when (simulator) {
            is Simulator.LmuWindows -> this is LmuWindowsReadoutListItemType
            is Simulator.Gt7Ps5 -> this is Gt7Ps5ReadoutListItemType
            is Simulator.AceWindows -> this is AceWindowsReadoutListItemType
        }

    companion object {
        fun fromId(
            simulator: Simulator,
            id: ReadoutItemKey,
        ): ReadoutListItemType? =
            when (simulator) {
                is Simulator.LmuWindows -> LmuWindowsReadoutListItemType.fromId(id)
                is Simulator.Gt7Ps5 -> Gt7Ps5ReadoutListItemType.fromId(id)
                is Simulator.AceWindows -> AceWindowsReadoutListItemType.fromId(id)
            }

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = defaultReadoutOrder(simulator)
    }
}
