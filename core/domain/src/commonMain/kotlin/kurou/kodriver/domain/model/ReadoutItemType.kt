package kurou.kodriver.domain.model

sealed class ReadoutItemType(val id: String) {
    data object VehicleApproach : ReadoutItemType("vehicle_approach")
    data object LapsRemaining : ReadoutItemType("laps_remaining")

    companion object {
        fun fromId(id: String): ReadoutItemType? = when (id) {
            "vehicle_approach" -> VehicleApproach
            "laps_remaining" -> LapsRemaining
            else -> null
        }
    }
}
