package kurou.kodriver.domain.model

sealed interface ReadoutItemKey {
    val value: String

    /**
     * listPane のトップレベル項目（Root）であることを表すマーカー。
     * キューへ積んで後で読み上げてよいかどうかは Root 単位でのみ判定するため、
     * supportsQueue は TopLevel にのみ存在し、サブ項目には存在しない。
     * デフォルト値は持たせず、新規追加時に必ず true/false を明示させる。
     */
    sealed interface TopLevel : ReadoutItemKey {
        val supportsQueue: Boolean
    }

    sealed interface LmuWindows : ReadoutItemKey {
        sealed interface TopLevel : LmuWindows, ReadoutItemKey.TopLevel

        sealed interface VehicleApproach : LmuWindows {
            data object Root : VehicleApproach, TopLevel {
                override val value = "lmu_windows_vehicle_approach"
                override val supportsQueue = false
            }
            data object Sustained : VehicleApproach { override val value = "lmu_windows_vehicle_approach_sustained" }
            data object StartReadout : VehicleApproach {
                override val value = "lmu_windows_vehicle_approach_start_readout"
            }
        }

        sealed interface MyBestLap : LmuWindows {
            data object Root : MyBestLap, TopLevel {
                override val value = "lmu_windows_my_best_lap"
                override val supportsQueue = true
            }
        }

        sealed interface Flag : LmuWindows {
            data object Root : Flag, TopLevel {
                override val value = "lmu_windows_flag"
                override val supportsQueue = true
            }
            data object BlueFlag : Flag { override val value = "lmu_windows_blue_flag" }
            data object SectorYellowFlag : Flag { override val value = "lmu_windows_sector_yellow_flag" }
            data object FullCourseYellow : Flag { override val value = "lmu_windows_full_course_yellow" }
            data object RedFlag : Flag { override val value = "lmu_windows_red_flag" }
        }

        sealed interface VehicleDamage : LmuWindows {
            data object Root : VehicleDamage, TopLevel {
                override val value = "lmu_windows_vehicle_damage"
                override val supportsQueue = true
            }
            data object Overheat : VehicleDamage { override val value = "lmu_windows_overheat" }
        }

        sealed interface TyreTemperature : LmuWindows {
            data object Root : TyreTemperature, TopLevel {
                override val value = "lmu_windows_tyre_temperature"
                override val supportsQueue = true
            }
            data object OverheatWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_overheat_warning"
            }
            data object LowWarning : TyreTemperature {
                override val value = "lmu_windows_tyre_temperature_low_warning"
            }
        }

        sealed interface PitTiming : LmuWindows {
            data object Root : PitTiming, TopLevel {
                override val value = "lmu_windows_pit_timing"
                override val supportsQueue = true
            }
        }

        sealed interface RemainingVirtualEnergy : LmuWindows {
            data object Root : RemainingVirtualEnergy, TopLevel {
                override val value = "lmu_windows_remaining_virtual_energy"
                override val supportsQueue = true
            }
        }

        sealed interface TyreWear : LmuWindows {
            data object Root : TyreWear, TopLevel {
                override val value = "lmu_windows_tyre_wear"
                override val supportsQueue = true
            }
        }
    }

    sealed interface Gt7Ps5 : ReadoutItemKey {
        sealed interface TopLevel : Gt7Ps5, ReadoutItemKey.TopLevel

        sealed interface MyBestLap : Gt7Ps5 {
            data object Root : MyBestLap, TopLevel {
                override val value = "gt7_ps5_my_best_lap"
                override val supportsQueue = true
            }
        }

        sealed interface RemainingFuelLaps : Gt7Ps5 {
            data object Root : RemainingFuelLaps, TopLevel {
                override val value = "gt7_ps5_remaining_fuel_laps"
                override val supportsQueue = true
            }
        }
    }

    sealed interface AceWindows : ReadoutItemKey {
        sealed interface TopLevel : AceWindows, ReadoutItemKey.TopLevel

        sealed interface RemainingFuel : AceWindows {
            data object Root : RemainingFuel, TopLevel {
                override val value = "ace_windows_remaining_fuel"
                override val supportsQueue = true
            }
        }
    }

    companion object {
        val entries by lazy {
            listOf(
                LmuWindows.VehicleApproach.Root,
                LmuWindows.VehicleApproach.Sustained,
                LmuWindows.VehicleApproach.StartReadout,
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
                LmuWindows.PitTiming.Root,
                LmuWindows.RemainingVirtualEnergy.Root,
                LmuWindows.TyreWear.Root,
                LmuWindows.MyBestLap.Root,
                Gt7Ps5.MyBestLap.Root,
                Gt7Ps5.RemainingFuelLaps.Root,
                AceWindows.RemainingFuel.Root,
            )
        }

        fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }
    }
}
