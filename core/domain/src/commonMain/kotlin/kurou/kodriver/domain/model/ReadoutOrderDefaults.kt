package kurou.kodriver.domain.model

/**
 * 読み上げ順序（優先度）の初期値。一覧画面と Narrator が同じ実効順序を使うため、`:core:domain` に置く。
 * 保存済みの順序が無い場合の順序であり、保存済み順序との突き合わせは `ResolveReadoutOrderUseCase` が行う。
 */
fun defaultReadoutOrder(simulator: Simulator): List<ReadoutItemKey> =
    when (simulator) {
        is Simulator.LmuWindows -> {
            ReadoutItemKey.entries
                .filterIsInstance<LmuWindowsReadoutItemKey.TopLevel>()
                .sortedBy { key -> lmuWindowsOrderIndex(key) }
        }

        is Simulator.Gt7Ps5 -> {
            ReadoutItemKey.entries
                .filterIsInstance<Gt7Ps5ReadoutItemKey.TopLevel>()
                .sortedBy { key -> gt7Ps5OrderIndex(key) }
        }

        is Simulator.AceWindows -> {
            ReadoutItemKey.entries
                .filterIsInstance<AceWindowsReadoutItemKey.TopLevel>()
                .sortedBy { key -> aceWindowsOrderIndex(key) }
        }
    }

// listPane のトップレベル項目のみ並び順を持つ。
// 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
private fun lmuWindowsOrderIndex(key: LmuWindowsReadoutItemKey.TopLevel): Int =
    when (key) {
        LmuWindowsReadoutItemKey.Flag.Root -> 0
        LmuWindowsReadoutItemKey.VehicleApproach.Root -> 1
        LmuWindowsReadoutItemKey.PitTiming.Root -> 2
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root -> 3
        LmuWindowsReadoutItemKey.TyreTemperature.Root -> 4
        LmuWindowsReadoutItemKey.TyreWear.Root -> 5
        LmuWindowsReadoutItemKey.BrakeTemperature.Root -> 6
        LmuWindowsReadoutItemKey.BrakeWear.Root -> 7
        LmuWindowsReadoutItemKey.VehicleDamage.Root -> 8
        LmuWindowsReadoutItemKey.MyBestLap.Root -> 9
    }

private fun gt7Ps5OrderIndex(key: Gt7Ps5ReadoutItemKey.TopLevel): Int =
    when (key) {
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root -> 0
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root -> 1
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root -> 2
        Gt7Ps5ReadoutItemKey.MyBestLap.Root -> 3
    }

private fun aceWindowsOrderIndex(key: AceWindowsReadoutItemKey.TopLevel): Int =
    when (key) {
        AceWindowsReadoutItemKey.Flag.Root -> 0
        AceWindowsReadoutItemKey.VehicleApproach.Root -> 1
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> 2
        AceWindowsReadoutItemKey.RemainingFuel.Root -> 3
        AceWindowsReadoutItemKey.TyreTemperature.Root -> 4
        AceWindowsReadoutItemKey.MyBestLap.Root -> 5
    }
