package kurou.kodriver.domain.model

/**
 * 読み上げ順序（優先度）の初期値。一覧画面と Narrator が同じ実効順序を使うため、`:core:domain` に置く。
 * 保存済みの順序が無い場合の順序であり、保存済み順序との突き合わせは `ResolveReadoutOrderUseCase` が行う。
 */
fun defaultReadoutOrder(simulator: Simulator): List<ReadoutItemKey> =
    when (simulator) {
        is Simulator.LmuWindows -> {
            ReadoutItemKey.entries
                .filterIsInstance<ReadoutItemKey.LmuWindows.TopLevel>()
                .sortedBy { key -> lmuWindowsOrderIndex(key) }
        }

        is Simulator.Gt7Ps5 -> {
            ReadoutItemKey.entries
                .filterIsInstance<ReadoutItemKey.Gt7Ps5.TopLevel>()
                .sortedBy { key -> gt7Ps5OrderIndex(key) }
        }

        is Simulator.AceWindows -> {
            ReadoutItemKey.entries
                .filterIsInstance<ReadoutItemKey.AceWindows.TopLevel>()
                .sortedBy { key -> aceWindowsOrderIndex(key) }
        }
    }

// listPane のトップレベル項目のみ並び順を持つ。
// 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
private fun lmuWindowsOrderIndex(key: ReadoutItemKey.LmuWindows.TopLevel): Int =
    when (key) {
        ReadoutItemKey.LmuWindows.Flag.Root -> 0
        ReadoutItemKey.LmuWindows.VehicleApproach.Root -> 1
        ReadoutItemKey.LmuWindows.PitTiming.Root -> 2
        ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> 3
        ReadoutItemKey.LmuWindows.TyreTemperature.Root -> 4
        ReadoutItemKey.LmuWindows.TyreWear.Root -> 5
        ReadoutItemKey.LmuWindows.BrakeTemperature.Root -> 6
        ReadoutItemKey.LmuWindows.BrakeWear.Root -> 7
        ReadoutItemKey.LmuWindows.VehicleDamage.Root -> 8
        ReadoutItemKey.LmuWindows.MyBestLap.Root -> 9
    }

private fun gt7Ps5OrderIndex(key: ReadoutItemKey.Gt7Ps5.TopLevel): Int =
    when (key) {
        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> 0
        ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> 1
        ReadoutItemKey.Gt7Ps5.TyreTemperature.Root -> 2
        ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> 3
    }

private fun aceWindowsOrderIndex(key: ReadoutItemKey.AceWindows.TopLevel): Int =
    when (key) {
        ReadoutItemKey.AceWindows.Flag.Root -> 0
        ReadoutItemKey.AceWindows.VehicleApproach.Root -> 1
        ReadoutItemKey.AceWindows.TyreTemperature.Root -> 2
        ReadoutItemKey.AceWindows.RemainingFuel.Root -> 3
        ReadoutItemKey.AceWindows.RemainingFuelLaps.Root -> 4
        ReadoutItemKey.AceWindows.MyBestLap.Root -> 5
    }
