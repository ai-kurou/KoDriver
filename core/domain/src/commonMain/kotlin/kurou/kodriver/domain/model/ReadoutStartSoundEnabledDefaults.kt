package kurou.kodriver.domain.model

// readout-list（ReadoutListViewModel）が参照するデフォルト値。
// 車両接近アナウンスは接近車両ごとに頻繁に再生されるため開始音をデフォルトOFFとし、
// それ以外の ReadoutItemKey.TopLevel はデフォルトONとする。
// 新しい ReadoutItemKey.TopLevel を追加した場合は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.VehicleApproach.Root to false,
        LmuWindowsReadoutItemKey.Flag.Root to true,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
        LmuWindowsReadoutItemKey.TyreWear.Root to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
        LmuWindowsReadoutItemKey.BrakeWear.Root to true,
        LmuWindowsReadoutItemKey.MyBestLap.Root to true,
        Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.Flag.Root to true,
        AceWindowsReadoutItemKey.VehicleApproach.Root to false,
        AceWindowsReadoutItemKey.RemainingFuel.Root to true,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
        AceWindowsReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.MyBestLap.Root to true,
    )
