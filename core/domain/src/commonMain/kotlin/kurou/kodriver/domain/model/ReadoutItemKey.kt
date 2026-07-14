package kurou.kodriver.domain.model

sealed interface ReadoutItemKey {
    val value: String

    sealed interface LmuWindows : ReadoutItemKey {
        sealed interface TopLevel : LmuWindows

        sealed interface VehicleApproach : LmuWindows {
            data object Root : VehicleApproach, TopLevel { override val value = "lmu_windows_vehicle_approach" }
            data object Sustained : VehicleApproach { override val value = "lmu_windows_vehicle_approach_sustained" }
        }

        sealed interface MyBestLap : LmuWindows {
            data object Root : MyBestLap, TopLevel { override val value = "lmu_windows_my_best_lap" }
        }

        sealed interface Flag : LmuWindows {
            data object Root : Flag, TopLevel { override val value = "lmu_windows_flag" }
            data object BlueFlag : Flag { override val value = "lmu_windows_blue_flag" }
            data object SectorYellowFlag : Flag { override val value = "lmu_windows_sector_yellow_flag" }
            data object FullCourseYellow : Flag { override val value = "lmu_windows_full_course_yellow" }
            data object RedFlag : Flag { override val value = "lmu_windows_red_flag" }
        }

        sealed interface VehicleDamage : LmuWindows {
            data object Root : VehicleDamage, TopLevel { override val value = "lmu_windows_vehicle_damage" }
            data object Overheat : VehicleDamage { override val value = "lmu_windows_overheat" }
        }

        sealed interface TyreTemperature : LmuWindows {
            data object Root : TyreTemperature, TopLevel { override val value = "lmu_windows_tyre_temperature" }
            data object OverheatWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_overheat_warning"
            }
            data object LowWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_low_warning"
            }
        }
    }

    sealed interface Gt7Ps5 : ReadoutItemKey {
        sealed interface TopLevel : Gt7Ps5

        sealed interface MyBestLap : Gt7Ps5 {
            data object Root : MyBestLap, TopLevel { override val value = "gt7_ps5_my_best_lap" }
        }

        sealed interface RemainingFuelLaps : Gt7Ps5 {
            data object Root : RemainingFuelLaps, TopLevel { override val value = "gt7_ps5_remaining_fuel_laps" }
        }
    }

    companion object {
        val entries by lazy {
            listOf(
                LmuWindows.VehicleApproach.Root,
                LmuWindows.VehicleApproach.Sustained,
                LmuWindows.Flag.Root,
                LmuWindows.Flag.BlueFlag,
                LmuWindows.Flag.SectorYellowFlag,
                LmuWindows.Flag.FullCourseYellow,
                LmuWindows.Flag.RedFlag,
                LmuWindows.VehicleDamage.Root,
                LmuWindows.VehicleDamage.Overheat,
                LmuWindows.TyreTemperature.Root,
                LmuWindows.TyreTemperature.OverheatWarning,
                LmuWindows.TyreTemperature.LowWarning,
                LmuWindows.MyBestLap.Root,
                Gt7Ps5.MyBestLap.Root,
                Gt7Ps5.RemainingFuelLaps.Root,
            )
        }

        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
