package kurou.kodriver.domain.model

public sealed interface Gt7Ps5ReadoutItemKey : ReadoutItemKey {
    public sealed interface TopLevel :
        Gt7Ps5ReadoutItemKey,
        ReadoutItemKey.TopLevel

    public sealed interface MyBestLap : Gt7Ps5ReadoutItemKey {
        public data object Root : MyBestLap, TopLevel {
            override val value: String = "gt7_ps5_auto_my_best_lap"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : MyBestLap {
            override val value: String = "gt7_ps5_auto_my_best_lap_detail_enabled"
        }
    }

    public sealed interface RemainingFuelLaps : Gt7Ps5ReadoutItemKey {
        public data object Root : RemainingFuelLaps, TopLevel {
            override val value: String = "gt7_ps5_auto_remaining_fuel_laps"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : RemainingFuelLaps {
            override val value: String = "gt7_ps5_auto_remaining_fuel_laps_detail_enabled"
        }
    }

    public sealed interface RemainingFuel : Gt7Ps5ReadoutItemKey {
        public data object Root : RemainingFuel, TopLevel {
            override val value: String = "gt7_ps5_auto_remaining_fuel"
            override val supportsQueue: Boolean = true
        }

        public data object DetailEnabled : RemainingFuel {
            override val value: String = "gt7_ps5_auto_remaining_fuel_detail_enabled"
        }
    }

    public sealed interface TyreTemperature : Gt7Ps5ReadoutItemKey {
        public data object Root : TyreTemperature, TopLevel {
            override val value: String = "gt7_ps5_auto_tyre_temperature"
            override val supportsQueue: Boolean = true
        }

        public data object OverheatWarning : TyreTemperature {
            override val value: String = "gt7_ps5_auto_tyre_temperature_overheat_warning"
        }
    }
}
