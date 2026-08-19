package kurou.kodriver.domain.model

// readout-list（ReadoutListViewModel）が参照するデフォルト値。
// 現行の「読み上げ開始音を必ず再生する」挙動を保つため、全ての ReadoutItemKey.TopLevel をデフォルト true とする。
// 新しい ReadoutItemKey.TopLevel を追加した場合は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
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
