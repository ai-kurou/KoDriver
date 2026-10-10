package kurou.kodriver.domain.model

sealed interface AceWindowsReadoutItemKey : ReadoutItemKey {
    sealed interface TopLevel :
        AceWindowsReadoutItemKey,
        ReadoutItemKey.TopLevel

    sealed interface VehicleApproach : AceWindowsReadoutItemKey {
        data object Root : VehicleApproach, TopLevel {
            override val value = "ace_windows_vehicle_approach"
            override val supportsQueue = false
        }

        data object StartReadout : VehicleApproach {
            override val value = "ace_windows_vehicle_approach_start_readout"
        }
    }

    sealed interface Flag : AceWindowsReadoutItemKey {
        data object Root : Flag, TopLevel {
            override val value = "ace_windows_flag"
            override val supportsQueue = true
        }

        data object WhiteFlag : Flag {
            override val value = "ace_windows_white_flag"
        }

        data object GreenFlag : Flag {
            override val value = "ace_windows_green_flag"
        }

        data object RedFlag : Flag {
            override val value = "ace_windows_red_flag"
        }

        data object BlueFlag : Flag {
            override val value = "ace_windows_blue_flag"
        }

        data object YellowFlag : Flag {
            override val value = "ace_windows_yellow_flag"
        }

        data object BlackFlag : Flag {
            override val value = "ace_windows_black_flag"
        }

        data object BlackWhiteFlag : Flag {
            override val value = "ace_windows_black_white_flag"
        }

        data object CheckeredFlag : Flag {
            override val value = "ace_windows_checkered_flag"
        }

        data object OrangeCircleFlag : Flag {
            override val value = "ace_windows_orange_circle_flag"
        }

        data object RedYellowStripesFlag : Flag {
            override val value = "ace_windows_red_yellow_stripes_flag"
        }
    }

    sealed interface RemainingFuel : AceWindowsReadoutItemKey {
        data object Root : RemainingFuel, TopLevel {
            override val value = "ace_windows_remaining_fuel"
            override val supportsQueue = true
        }

        data object DetailEnabled : RemainingFuel {
            override val value = "ace_windows_remaining_fuel_detail_enabled"
        }
    }

    sealed interface RemainingFuelLaps : AceWindowsReadoutItemKey {
        data object Root : RemainingFuelLaps, TopLevel {
            override val value = "ace_windows_remaining_fuel_laps"
            override val supportsQueue = true
        }

        data object DetailEnabled : RemainingFuelLaps {
            override val value = "ace_windows_remaining_fuel_laps_detail_enabled"
        }
    }

    sealed interface MyBestLap : AceWindowsReadoutItemKey {
        data object Root : MyBestLap, TopLevel {
            override val value = "ace_windows_my_best_lap"
            override val supportsQueue = true
        }

        data object DetailEnabled : MyBestLap {
            override val value = "ace_windows_my_best_lap_detail_enabled"
        }
    }

    sealed interface TyreTemperature : AceWindowsReadoutItemKey {
        data object Root : TyreTemperature, TopLevel {
            override val value = "ace_windows_tyre_temperature"
            override val supportsQueue = true
        }

        data object OverheatWarning : TyreTemperature {
            override val value = "ace_windows_tyre_temperature_overheat_warning"
        }
    }
}
