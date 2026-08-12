package kurou.kodriver.domain.model

// listPane（ReadoutListViewModel）が参照するデフォルト値。
// supportsQueue が true の ReadoutItemKey.TopLevel は必ずここに列挙すること（省略＝デフォルトfalse、ではない）。
val QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.LmuWindows.Flag.Root to false,
        ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
        ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
        ReadoutItemKey.LmuWindows.PitTiming.Root to true,
        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
        ReadoutItemKey.LmuWindows.TyreWear.Root to true,
        ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
        ReadoutItemKey.Gt7Ps5.MyBestLap.Root to false,
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
        ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to true,
        ReadoutItemKey.AceWindows.Flag.Root to true,
        ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
    )
