package kurou.kodriver.domain.model

// readout-list（ReadoutListViewModel）が参照するデフォルト値。
// 車両接近アナウンスは接近車両ごとに頻繁に再生されるため開始音をデフォルトOFFとし、
// それ以外の ReadoutItemKey.TopLevel はデフォルトONとする。
// 新しい ReadoutItemKey.TopLevel を追加した場合は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.LmuWindows.VehicleApproach.Root to false,
        ReadoutItemKey.LmuWindows.Flag.Root to true,
        ReadoutItemKey.LmuWindows.VehicleDamage.Root to true,
        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
        ReadoutItemKey.LmuWindows.PitTiming.Root to true,
        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to true,
        ReadoutItemKey.LmuWindows.TyreWear.Root to true,
        ReadoutItemKey.LmuWindows.MyBestLap.Root to true,
        ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
        ReadoutItemKey.Gt7Ps5.TyreTemperature.Root to true,
        ReadoutItemKey.AceWindows.Flag.Root to true,
        ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
        ReadoutItemKey.AceWindows.TyreTemperature.Root to true,
    )
