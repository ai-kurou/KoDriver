package kurou.kodriver.domain.model

// listPane（ReadoutListViewModel）が参照するデフォルト値。
// supportsQueue が true の ReadoutItemKey.TopLevel は必ずここに列挙すること（省略＝デフォルトfalse、ではない）。
val QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.Flag.Root to false,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
        LmuWindowsReadoutItemKey.TyreWear.Root to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
        LmuWindowsReadoutItemKey.BrakeWear.Root to true,
        LmuWindowsReadoutItemKey.MyBestLap.Root to false,
        Gt7Ps5ReadoutItemKey.MyBestLap.Root to false,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.Flag.Root to false,
        AceWindowsReadoutItemKey.RemainingFuel.Root to true,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
        AceWindowsReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.MyBestLap.Root to false,
    )
