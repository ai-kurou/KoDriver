package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.LmuWindows.VehicleApproach)
        data object Flag : LmuWindows(ReadoutItemKey.LmuWindows.Flag)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.LmuWindows.VehicleDamage)
        data object TyreTemperature : LmuWindows(ReadoutItemKey.LmuWindows.TyreTemperature)
        data object MyBestLap : LmuWindows(ReadoutItemKey.LmuWindows.MyBestLap)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(ReadoutItemKey.Gt7Ps5.MyBestLap)
        data object RemainingFuelLaps : Gt7Ps5(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps)
    }

    companion object {
        fun fromId(simulator: Simulator, id: ReadoutItemKey): ReadoutListItemType? = when (simulator) {
            is Simulator.LmuWindows -> when (id) {
                ReadoutItemKey.LmuWindows.VehicleApproach -> LmuWindows.VehicleApproach
                ReadoutItemKey.LmuWindows.Flag -> LmuWindows.Flag
                ReadoutItemKey.LmuWindows.VehicleDamage -> LmuWindows.VehicleDamage
                ReadoutItemKey.LmuWindows.TyreTemperature -> LmuWindows.TyreTemperature
                ReadoutItemKey.LmuWindows.MyBestLap -> LmuWindows.MyBestLap
                else -> null
            }
            is Simulator.Gt7Ps5 -> when (id) {
                ReadoutItemKey.Gt7Ps5.MyBestLap -> Gt7Ps5.MyBestLap
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps -> Gt7Ps5.RemainingFuelLaps
                else -> null
            }
        }

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = when (simulator) {
            is Simulator.LmuWindows -> {
                ReadoutItemKey.entries
                    .filterIsInstance<ReadoutItemKey.LmuWindows>()
                    .mapNotNull { key -> lmuWindowsOrderIndex(key)?.let { key to it } }
                    .sortedBy { (_, orderIndex) -> orderIndex }
                    .map { (key, _) -> key }
            }
            is Simulator.Gt7Ps5 -> {
                ReadoutItemKey.entries
                    .filterIsInstance<ReadoutItemKey.Gt7Ps5>()
                    .sortedBy { key -> gt7Ps5OrderIndex(key) }
            }
        }

        // listPane のトップレベル項目のみ並び順を持つ。detailPane のサブトグル専用キーは null を返す。
        // 新しい ReadoutItemKey.LmuWindows を追加した際、ここで対応を判断しないとコンパイルが通らない。
        private fun lmuWindowsOrderIndex(key: ReadoutItemKey.LmuWindows): Int? = when (key) {
            ReadoutItemKey.LmuWindows.Flag -> 0
            ReadoutItemKey.LmuWindows.VehicleApproach -> 1
            ReadoutItemKey.LmuWindows.VehicleDamage -> 2
            ReadoutItemKey.LmuWindows.TyreTemperature -> 3
            ReadoutItemKey.LmuWindows.MyBestLap -> 4
            ReadoutItemKey.LmuWindows.BlueFlag,
            ReadoutItemKey.LmuWindows.SectorYellowFlag,
            ReadoutItemKey.LmuWindows.FullCourseYellow,
            ReadoutItemKey.LmuWindows.RedFlag,
            ReadoutItemKey.LmuWindows.Overheat,
            -> null
        }

        private fun gt7Ps5OrderIndex(key: ReadoutItemKey.Gt7Ps5): Int = when (key) {
            ReadoutItemKey.Gt7Ps5.RemainingFuelLaps -> 0
            ReadoutItemKey.Gt7Ps5.MyBestLap -> 1
        }
    }
}
