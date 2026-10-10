package kurou.kodriver.domain.model

sealed interface LmuWindowsReadoutItemKey : ReadoutItemKey {
    sealed interface TopLevel :
        LmuWindowsReadoutItemKey,
        ReadoutItemKey.TopLevel

    sealed interface VehicleApproach : LmuWindowsReadoutItemKey {
        data object Root : VehicleApproach, TopLevel {
            override val value = "lmu_windows_vehicle_approach"
            override val supportsQueue = false
        }

        data object Sustained : VehicleApproach {
            override val value = "lmu_windows_vehicle_approach_sustained"
        }

        data object StartReadout : VehicleApproach {
            override val value = "lmu_windows_vehicle_approach_start_readout"
        }
    }

    sealed interface MyBestLap : LmuWindowsReadoutItemKey {
        data object Root : MyBestLap, TopLevel {
            override val value = "lmu_windows_my_best_lap"
            override val supportsQueue = true
        }

        data object DetailEnabled : MyBestLap {
            override val value = "lmu_windows_my_best_lap_detail_enabled"
        }
    }

    sealed interface Flag : LmuWindowsReadoutItemKey {
        data object Root : Flag, TopLevel {
            override val value = "lmu_windows_flag"
            override val supportsQueue = true
        }

        data object BlueFlag : Flag {
            override val value = "lmu_windows_blue_flag"
        }

        data object SectorYellowFlag : Flag {
            override val value = "lmu_windows_sector_yellow_flag"
        }

        data object FullCourseYellow : Flag {
            override val value = "lmu_windows_full_course_yellow"
        }

        data object RedFlag : Flag {
            override val value = "lmu_windows_red_flag"
        }
    }

    sealed interface VehicleDamage : LmuWindowsReadoutItemKey {
        data object Root : VehicleDamage, TopLevel {
            override val value = "lmu_windows_vehicle_damage"
            override val supportsQueue = true
        }

        data object Overheat : VehicleDamage {
            override val value = "lmu_windows_overheat"
        }

        data object PartDetached : VehicleDamage {
            override val value = "lmu_windows_part_detached"
        }

        data object TyreDetached : VehicleDamage {
            override val value = "lmu_windows_tyre_detached"
        }
    }

    sealed interface TyreTemperature : LmuWindowsReadoutItemKey {
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

    sealed interface PitTiming : LmuWindowsReadoutItemKey {
        data object Root : PitTiming, TopLevel {
            override val value = "lmu_windows_pit_timing"
            override val supportsQueue = true
        }

        data object VirtualEnergy : PitTiming {
            override val value = "lmu_windows_pit_timing_virtual_energy"
        }

        data object TyreWear : PitTiming {
            override val value = "lmu_windows_pit_timing_tyre_wear"
        }
    }

    sealed interface RemainingVirtualEnergy : LmuWindowsReadoutItemKey {
        data object Root : RemainingVirtualEnergy, TopLevel {
            override val value = "lmu_windows_remaining_virtual_energy"
            override val supportsQueue = true
        }

        data object WarningReadout : RemainingVirtualEnergy {
            override val value = "lmu_windows_remaining_virtual_energy_warning_readout"
        }
    }

    sealed interface TyreWear : LmuWindowsReadoutItemKey {
        data object Root : TyreWear, TopLevel {
            override val value = "lmu_windows_tyre_wear"
            override val supportsQueue = true
        }

        data object WarningReadout : TyreWear {
            override val value = "lmu_windows_tyre_wear_warning_readout"
        }
    }

    sealed interface BrakeTemperature : LmuWindowsReadoutItemKey {
        data object Root : BrakeTemperature, TopLevel {
            override val value = "lmu_windows_brake_temperature"
            override val supportsQueue = true
        }

        data object WarningReadout : BrakeTemperature {
            override val value = "lmu_windows_brake_temperature_warning_readout"
        }
    }

    sealed interface BrakeWear : LmuWindowsReadoutItemKey {
        data object Root : BrakeWear, TopLevel {
            override val value = "lmu_windows_brake_wear"
            override val supportsQueue = true
        }

        data object WarningReadout : BrakeWear {
            override val value = "lmu_windows_brake_wear_warning_readout"
        }
    }
}
