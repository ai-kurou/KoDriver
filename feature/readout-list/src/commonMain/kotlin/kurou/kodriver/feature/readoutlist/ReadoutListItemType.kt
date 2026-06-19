package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    data object VehicleApproach : ReadoutListItemType(ReadoutItemKey.VEHICLE_APPROACH)
    data object Flag : ReadoutListItemType(ReadoutItemKey.FLAG)
    data object VehicleDamage : ReadoutListItemType(ReadoutItemKey.VEHICLE_DAMAGE)

    companion object {
        private val entries = listOf(VehicleApproach, Flag, VehicleDamage)

        fun fromId(id: ReadoutItemKey): ReadoutListItemType? = entries.find { it.id == id }
    }
}
