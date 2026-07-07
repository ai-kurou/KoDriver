package kurou.kodriver.domain.model

sealed interface ReadoutItemKey {
    val value: String

    sealed interface LmuWindows : ReadoutItemKey {
        data object VehicleApproach : LmuWindows { override val value = "lmu_windows_vehicle_approach" }
        data object Flag : LmuWindows { override val value = "lmu_windows_flag" }
        data object BlueFlag : LmuWindows { override val value = "lmu_windows_blue_flag" }
        data object SectorYellowFlag : LmuWindows { override val value = "lmu_windows_sector_yellow_flag" }
        data object FullCourseYellow : LmuWindows { override val value = "lmu_windows_full_course_yellow" }
        data object RedFlag : LmuWindows { override val value = "lmu_windows_red_flag" }
        data object VehicleDamage : LmuWindows { override val value = "lmu_windows_vehicle_damage" }
        data object Overheat : LmuWindows { override val value = "lmu_windows_overheat" }
        data object TyreTemperature : LmuWindows { override val value = "lmu_windows_tyre_temperature" }
        data object MyBestLap : LmuWindows { override val value = "lmu_windows_my_best_lap" }
    }

    sealed interface Gt7Ps5 : ReadoutItemKey {
        data object MyBestLap : Gt7Ps5 { override val value = "gt7_ps5_my_best_lap" }
        data object RemainingFuelLaps : Gt7Ps5 { override val value = "gt7_ps5_remaining_fuel_laps" }
    }

    companion object {
        private val entries by lazy {
            listOf(
                LmuWindows.VehicleApproach,
                LmuWindows.Flag,
                LmuWindows.BlueFlag,
                LmuWindows.SectorYellowFlag,
                LmuWindows.FullCourseYellow,
                LmuWindows.RedFlag,
                LmuWindows.VehicleDamage,
                LmuWindows.Overheat,
                LmuWindows.TyreTemperature,
                LmuWindows.MyBestLap,
                Gt7Ps5.MyBestLap,
                Gt7Ps5.RemainingFuelLaps,
            )
        }

        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
