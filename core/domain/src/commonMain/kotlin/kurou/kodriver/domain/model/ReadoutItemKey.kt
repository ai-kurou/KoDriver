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
    data object BestLap : ReadoutItemKey { override val value = "best_lap" }

    companion object {
        val VEHICLE_APPROACH: ReadoutItemKey = VehicleApproach
        val FLAG: ReadoutItemKey = Flag
        val BLUE_FLAG: ReadoutItemKey = BlueFlag
        val SECTOR_YELLOW_FLAG: ReadoutItemKey = SectorYellowFlag
        val FULL_COURSE_YELLOW: ReadoutItemKey = FullCourseYellow
        val RED_FLAG: ReadoutItemKey = RedFlag
        val VEHICLE_DAMAGE: ReadoutItemKey = VehicleDamage
        val OVERHEAT: ReadoutItemKey = Overheat
        val BEST_LAP: ReadoutItemKey = BestLap

        private val entries = listOf(
            VEHICLE_APPROACH,
            FLAG,
            BLUE_FLAG,
            SECTOR_YELLOW_FLAG,
            FULL_COURSE_YELLOW,
            RED_FLAG,
            VEHICLE_DAMAGE,
            OVERHEAT,
            BEST_LAP,
        )

        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
