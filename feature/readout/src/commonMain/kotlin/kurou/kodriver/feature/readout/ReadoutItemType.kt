package kurou.kodriver.feature.readout

sealed class ReadoutItemType(val id: String) {
    data object VehicleApproach : ReadoutItemType("vehicle_approach")
    data object LapsRemaining : ReadoutItemType("laps_remaining")

    companion object {
        private val entries = listOf(VehicleApproach, LapsRemaining)

        fun fromId(id: String): ReadoutItemType? = entries.find { it.id == id }
    }
}
