package kurou.kodriver.domain.model

// listPane（ReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照するための、シミュレーターごとのデフォルト有効状態。
// listPaneに表示される ReadoutItemKey は必ずここに列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_ENABLED_STATE_DEFAULT: Map<Simulator, Map<ReadoutItemKey, Boolean>> = mapOf(
    Simulator.LmuWindows to mapOf(
        ReadoutItemKey.LmuWindows.Flag.Root to true,
        ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
        ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
        ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
        ReadoutItemKey.LmuWindows.PitTiming.Root to true,
        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to false,
        ReadoutItemKey.LmuWindows.TyreWear.Root to false,
        ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
    ),
    Simulator.Gt7Ps5 to mapOf(
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
        ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
    ),
    Simulator.AceWindows to mapOf(
        ReadoutItemKey.AceWindows.Flag.Root to true,
        ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
    ),
)
