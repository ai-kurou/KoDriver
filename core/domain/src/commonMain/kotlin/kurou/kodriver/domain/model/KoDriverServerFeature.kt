package kurou.kodriver.domain.model

enum class KoDriverServerFeature(
    private val pathSegment: String,
) {
    FLAGS(pathSegment = "flags"),
    PROXIMITY(pathSegment = "proximity"),
    DAMAGE(pathSegment = "damage"),
    TYRE_CARCASS_TEMPERATURE(pathSegment = "tyre_carcass_temperature"),
    MY_BEST_LAP(pathSegment = "my_best_lap"),
    ;

    fun webSocketPath(simulator: Simulator): String =
        "/ws/${simulator.id}/$pathSegment"
}
