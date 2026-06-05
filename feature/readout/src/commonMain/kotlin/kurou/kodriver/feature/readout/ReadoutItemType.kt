package kurou.kodriver.feature.readout

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutItemType(val id: String) {
    data object VehicleApproach : ReadoutItemType(ReadoutItemKey.VEHICLE_APPROACH)
    data object LapsRemaining : ReadoutItemType(ReadoutItemKey.LAPS_REMAINING)

    companion object {
        private val entries = listOf(VehicleApproach, LapsRemaining)

        fun fromId(id: String): ReadoutItemType? = entries.find { it.id == id }
    }
}
