package kurou.kodriver.domain.model

sealed interface ReadoutItemKey {
    val value: String

    data object VehicleApproach : ReadoutItemKey { override val value = "vehicle_approach" }
    data object Flag : ReadoutItemKey { override val value = "flag" }
    data object BlueFlag : ReadoutItemKey { override val value = "blue_flag" }
    data object SectorYellowFlag : ReadoutItemKey { override val value = "sector_yellow_flag" }
    data object FullCourseYellow : ReadoutItemKey { override val value = "full_course_yellow" }
    data object RedFlag : ReadoutItemKey { override val value = "red_flag" }
    data object VehicleDamage : ReadoutItemKey { override val value = "vehicle_damage" }
    data object Overheat : ReadoutItemKey { override val value = "overheat" }
    data object MyBestLap : ReadoutItemKey { override val value = "my_best_lap" }
    data object RemainingFuelLaps : ReadoutItemKey { override val value = "remaining_fuel_laps" }

    companion object {
        private val entries by lazy {
            listOf(
                VehicleApproach,
                Flag,
                BlueFlag,
                SectorYellowFlag,
                FullCourseYellow,
                RedFlag,
                VehicleDamage,
                Overheat,
                MyBestLap,
                RemainingFuelLaps,
            )
        }

        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
