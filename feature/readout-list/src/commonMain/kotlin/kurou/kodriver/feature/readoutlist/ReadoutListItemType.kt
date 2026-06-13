package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutListItemType(val id: String) {
    data object VehicleApproach : ReadoutListItemType(ReadoutItemKey.VEHICLE_APPROACH)
    data object Flag : ReadoutListItemType(ReadoutItemKey.FLAG)
    data object VehicleDamage : ReadoutListItemType(ReadoutItemKey.VEHICLE_DAMAGE)

    companion object {
        private val entries = listOf(VehicleApproach, Flag, VehicleDamage)

        fun fromId(id: String): ReadoutListItemType? = entries.find { it.id == id }
    }
}
