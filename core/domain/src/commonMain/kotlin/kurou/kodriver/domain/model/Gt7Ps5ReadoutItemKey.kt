package kurou.kodriver.domain.model

sealed interface Gt7Ps5ReadoutItemKey : ReadoutItemKey {
    sealed interface TopLevel :
        Gt7Ps5ReadoutItemKey,
        ReadoutItemKey.TopLevel

    sealed interface MyBestLap : Gt7Ps5ReadoutItemKey {
        data object Root : MyBestLap, TopLevel {
            override val value = "gt7_ps5_my_best_lap"
            override val supportsQueue = true
        }

        data object DetailEnabled : MyBestLap {
            override val value = "gt7_ps5_my_best_lap_detail_enabled"
        }
    }

    sealed interface RemainingFuelLaps : Gt7Ps5ReadoutItemKey {
        data object Root : RemainingFuelLaps, TopLevel {
            override val value = "gt7_ps5_remaining_fuel_laps"
            override val supportsQueue = true
        }

        data object DetailEnabled : RemainingFuelLaps {
            override val value = "gt7_ps5_remaining_fuel_laps_detail_enabled"
        }
    }

    sealed interface RemainingFuel : Gt7Ps5ReadoutItemKey {
        data object Root : RemainingFuel, TopLevel {
            override val value = "gt7_ps5_remaining_fuel"
            override val supportsQueue = true
        }

        data object DetailEnabled : RemainingFuel {
            override val value = "gt7_ps5_remaining_fuel_detail_enabled"
        }
    }

    sealed interface TyreTemperature : Gt7Ps5ReadoutItemKey {
        data object Root : TyreTemperature, TopLevel {
            override val value = "gt7_ps5_tyre_temperature"
            override val supportsQueue = true
        }

        data object OverheatWarning : TyreTemperature {
            override val value = "gt7_ps5_tyre_temperature_overheat_warning"
        }
    }
}
