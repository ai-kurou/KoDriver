package kurou.kodriver.domain.model

enum class VehicleApproachSustainedReadoutType(
    val id: String,
) {
    KEEP_LEFT_RIGHT("keep_left_right"),
    LEFT_RIGHT_SUSTAINED("left_right_sustained"),
    ;

    companion object {
        fun fromId(id: String): VehicleApproachSustainedReadoutType =
            entries.firstOrNull { it.id == id } ?: LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_READOUT_TYPE_DEFAULT
    }
}
