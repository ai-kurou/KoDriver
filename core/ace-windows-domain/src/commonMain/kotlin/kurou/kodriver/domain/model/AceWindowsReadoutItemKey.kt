package kurou.kodriver.domain.model

public sealed interface AceWindowsReadoutItemKey : ReadoutItemKey {
    public sealed interface TopLevel :
        AceWindowsReadoutItemKey,
        ReadoutItemKey.TopLevel

    public sealed interface VehicleApproach : AceWindowsReadoutItemKey {
        public data object Root : VehicleApproach, TopLevel {
            override val value: String = "ace_windows_auto_vehicle_approach"
            override val supportsQueue: Boolean = false
        }

        public data object StartReadout : VehicleApproach {
            override val value: String = "ace_windows_auto_vehicle_approach_start_readout"
        }
    }

    public sealed interface Flag : AceWindowsReadoutItemKey {
        public data object Root : Flag, TopLevel {
            override val value: String = "ace_windows_auto_flag"
            override val supportsQueue: Boolean = true
        }

        public data object WhiteFlag : Flag {
            override val value: String = "ace_windows_auto_white_flag"
        }

        public data object GreenFlag : Flag {
            override val value: String = "ace_windows_auto_green_flag"
        }

        public data object RedFlag : Flag {
            override val value: String = "ace_windows_auto_red_flag"
        }

        public data object BlueFlag : Flag {
            override val value: String = "ace_windows_auto_blue_flag"
        }

        public data object YellowFlag : Flag {
            override val value: String = "ace_windows_auto_yellow_flag"
        }

        public data object BlackFlag : Flag {
            override val value: String = "ace_windows_auto_black_flag"
        }

        public data object BlackWhiteFlag : Flag {
            override val value: String = "ace_windows_auto_black_white_flag"
        }

        public data object CheckeredFlag : Flag {
            override val value: String = "ace_windows_auto_checkered_flag"
        }

        public data object OrangeCircleFlag : Flag {
            override val value: String = "ace_windows_auto_orange_circle_flag"
        }

        public data object RedYellowStripesFlag : Flag {
            override val value: String = "ace_windows_auto_red_yellow_stripes_flag"
        }
    }

    public sealed interface RemainingFuel : AceWindowsReadoutItemKey {
        public data object Root : RemainingFuel, TopLevel {
            override val value: String = "ace_windows_auto_remaining_fuel"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : RemainingFuel {
            override val value: String = "ace_windows_auto_remaining_fuel_detail_enabled"
        }
    }

    public sealed interface RemainingFuelLaps : AceWindowsReadoutItemKey {
        public data object Root : RemainingFuelLaps, TopLevel {
            override val value: String = "ace_windows_auto_remaining_fuel_laps"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : RemainingFuelLaps {
            override val value: String = "ace_windows_auto_remaining_fuel_laps_detail_enabled"
        }
    }

    public sealed interface MyBestLap : AceWindowsReadoutItemKey {
        public data object Root : MyBestLap, TopLevel {
            override val value: String = "ace_windows_auto_my_best_lap"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : MyBestLap {
            override val value: String = "ace_windows_auto_my_best_lap_detail_enabled"
        }
    }

    public sealed interface TyreTemperature : AceWindowsReadoutItemKey {
        public data object Root : TyreTemperature, TopLevel {
            override val value: String = "ace_windows_auto_tyre_temperature"
            override val supportsQueue: Boolean = true
        }

        public data object OverheatWarning : TyreTemperature {
            override val value: String = "ace_windows_auto_tyre_temperature_overheat_warning"
        }
    }

    public sealed interface BrakeTemperature : AceWindowsReadoutItemKey {
        public data object Root : BrakeTemperature, TopLevel {
            override val value: String = "ace_windows_auto_brake_temperature"
            override val supportsQueue: Boolean = true
        }
    }

    /** 手動読み上げ専用の前後車ギャップ項目。自動読み上げのスイッチは持たない。 */
    public sealed interface CarGap : AceWindowsReadoutItemKey {
        public data object Root : CarGap {
            override val value: String = "ace_windows_auto_car_gap"
        }
    }
}
