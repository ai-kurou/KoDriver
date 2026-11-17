package kurou.kodriver.domain.model

public sealed interface LmuWindowsReadoutItemKey : ReadoutItemKey {
    public sealed interface TopLevel :
        LmuWindowsReadoutItemKey,
        ReadoutItemKey.TopLevel

    public sealed interface VehicleApproach : LmuWindowsReadoutItemKey {
        public data object Root : VehicleApproach, TopLevel {
            override val value: String = "lmu_windows_auto_vehicle_approach"
            override val supportsQueue: Boolean = false
        }

        public data object Sustained : VehicleApproach {
            override val value: String = "lmu_windows_auto_vehicle_approach_sustained"
        }

        public data object StartReadout : VehicleApproach {
            override val value: String = "lmu_windows_auto_vehicle_approach_start_readout"
        }
    }

    public sealed interface MyBestLap : LmuWindowsReadoutItemKey {
        public data object Root : MyBestLap, TopLevel {
            override val value: String = "lmu_windows_auto_my_best_lap"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : MyBestLap {
            override val value: String = "lmu_windows_auto_my_best_lap_detail_enabled"
        }
    }

    public sealed interface Flag : LmuWindowsReadoutItemKey {
        public data object Root : Flag, TopLevel {
            override val value: String = "lmu_windows_auto_flag"
            override val supportsQueue: Boolean = true
        }

        public data object BlueFlag : Flag {
            override val value: String = "lmu_windows_auto_blue_flag"
        }

        public data object SectorYellowFlag : Flag {
            override val value: String = "lmu_windows_auto_sector_yellow_flag"
        }

        public data object FullCourseYellow : Flag {
            override val value: String = "lmu_windows_auto_full_course_yellow"
        }

        public data object RedFlag : Flag {
            override val value: String = "lmu_windows_auto_red_flag"
        }
    }

    public sealed interface VehicleDamage : LmuWindowsReadoutItemKey {
        public data object Root : VehicleDamage, TopLevel {
            override val value: String = "lmu_windows_auto_vehicle_damage"
            override val supportsQueue: Boolean = true
        }

        public data object Overheat : VehicleDamage {
            override val value: String = "lmu_windows_auto_overheat"
        }

        public data object PartDetached : VehicleDamage {
            override val value: String = "lmu_windows_auto_part_detached"
        }

        public data object TyreDetached : VehicleDamage {
            override val value: String = "lmu_windows_auto_tyre_detached"
        }
    }

    public sealed interface TyreTemperature : LmuWindowsReadoutItemKey {
        public data object Root : TyreTemperature, TopLevel {
            override val value: String = "lmu_windows_auto_tyre_temperature"
            override val supportsQueue: Boolean = true
        }

        public data object OverheatWarning : TyreTemperature {
            override val value: String = "lmu_windows_auto_tyre_temperature_overheat_warning"
        }

        public data object LowWarning : TyreTemperature {
            override val value: String = "lmu_windows_auto_tyre_temperature_low_warning"
        }
    }

    public sealed interface PitTiming : LmuWindowsReadoutItemKey {
        public data object Root : PitTiming, TopLevel {
            override val value: String = "lmu_windows_auto_pit_timing"
            override val supportsQueue: Boolean = true
        }

        public data object VirtualEnergy : PitTiming {
            override val value: String = "lmu_windows_auto_pit_timing_virtual_energy"
        }

        public data object TyreWear : PitTiming {
            override val value: String = "lmu_windows_auto_pit_timing_tyre_wear"
        }

        public data object Fuel : PitTiming {
            override val value: String = "lmu_windows_auto_pit_timing_fuel"
        }
    }

    public sealed interface RemainingVirtualEnergy : LmuWindowsReadoutItemKey {
        public data object Root : RemainingVirtualEnergy, TopLevel {
            override val value: String = "lmu_windows_auto_remaining_virtual_energy"
            override val supportsQueue: Boolean = true
        }

        public data object WarningReadout : RemainingVirtualEnergy {
            override val value: String = "lmu_windows_auto_remaining_virtual_energy_warning_readout"
        }
    }

    public sealed interface RemainingFuel : LmuWindowsReadoutItemKey {
        public data object Root : RemainingFuel, TopLevel {
            override val value: String = "lmu_windows_auto_remaining_fuel"
            override val supportsQueue: Boolean = true
        }

        public data object WarningReadout : RemainingFuel {
            override val value: String = "lmu_windows_auto_remaining_fuel_warning_readout"
        }

        public data object Laps : RemainingFuel {
            override val value: String = "lmu_windows_auto_remaining_fuel_laps"
        }
    }

    public sealed interface TyreWear : LmuWindowsReadoutItemKey {
        public data object Root : TyreWear, TopLevel {
            override val value: String = "lmu_windows_auto_tyre_wear"
            override val supportsQueue: Boolean = true
        }

        public data object WarningReadout : TyreWear {
            override val value: String = "lmu_windows_auto_tyre_wear_warning_readout"
        }
    }

    public sealed interface BrakeTemperature : LmuWindowsReadoutItemKey {
        public data object Root : BrakeTemperature, TopLevel {
            override val value: String = "lmu_windows_auto_brake_temperature"
            override val supportsQueue: Boolean = true
        }

        public data object WarningReadout : BrakeTemperature {
            override val value: String = "lmu_windows_auto_brake_temperature_warning_readout"
        }
    }

    public sealed interface BrakeWear : LmuWindowsReadoutItemKey {
        public data object Root : BrakeWear, TopLevel {
            override val value: String = "lmu_windows_auto_brake_wear"
            override val supportsQueue: Boolean = true
        }

        public data object WarningReadout : BrakeWear {
            override val value: String = "lmu_windows_auto_brake_wear_warning_readout"
        }
    }

    public sealed interface ClassCarGap : LmuWindowsReadoutItemKey {
        public data object Root : ClassCarGap {
            override val value: String = "lmu_windows_auto_class_car_gap"
        }
    }
}
