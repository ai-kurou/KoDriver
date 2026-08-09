package kurou.kodriver.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleApproachStartReadoutType(
    val id: String,
) {
    CAR_LEFT_RIGHT("car_left_right"),
    LEFT_RIGHT_APPROACH("left_right_approach"),
    ;

    companion object {
        fun fromId(id: String): VehicleApproachStartReadoutType? = entries.firstOrNull { it.id == id }
    }
}
