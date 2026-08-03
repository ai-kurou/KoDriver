package kurou.kodriver.domain.model

enum class KoDriverServerFeature(
    private val pathSegment: String,
) {
    FLAGS(pathSegment = "flags"),
    VEHICLE_APPROACH(pathSegment = "vehicle_approach"),
    DAMAGE(pathSegment = "damage"),
    TYRE_CARCASS_TEMPERATURE(pathSegment = "tyre_carcass_temperature"),
    VEHICLE_CLASS(pathSegment = "vehicle_class"),
    TYRE_WEAR(pathSegment = "tyre_wear"),
    MY_BEST_LAP(pathSegment = "my_best_lap"),
    VIRTUAL_ENERGY(pathSegment = "virtual_energy"),
    FUEL(pathSegment = "fuel"),
    STATUS(pathSegment = "status"),
    ;

    fun webSocketPath(simulator: Simulator): String = "/ws/${simulator.id}/$pathSegment"
}
