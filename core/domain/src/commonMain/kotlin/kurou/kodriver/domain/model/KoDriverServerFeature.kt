package kurou.kodriver.domain.model

enum class KoDriverServerFeature(
    private val pathSegment: String,
) {
    FLAGS(pathSegment = "flags"),
    PROXIMITY(pathSegment = "proximity"),
    DAMAGE(pathSegment = "damage"),
    ;

    fun webSocketPath(simulator: Simulator): String =
        "/ws/${simulator.id}/$pathSegment"
}
