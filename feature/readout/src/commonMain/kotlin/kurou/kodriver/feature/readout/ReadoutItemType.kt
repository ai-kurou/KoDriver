package kurou.kodriver.feature.readout

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutItemType(val id: String) {
    data object VehicleApproach : ReadoutItemType(ReadoutItemKey.VEHICLE_APPROACH)
    data object Flag : ReadoutItemType(ReadoutItemKey.FLAG)
    data object VehicleDamage : ReadoutItemType(ReadoutItemKey.VEHICLE_DAMAGE)

    companion object {
        private val entries = listOf(VehicleApproach, Flag, VehicleDamage)

        fun fromId(id: String): ReadoutItemType? = entries.find { it.id == id }
    }
}
