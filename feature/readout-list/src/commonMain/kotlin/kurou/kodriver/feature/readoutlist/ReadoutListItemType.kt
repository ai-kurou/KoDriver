package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.LmuWindows.VehicleApproach.Root)
        data object Flag : LmuWindows(ReadoutItemKey.LmuWindows.Flag.Root)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.LmuWindows.VehicleDamage.Root)
        data object TyreTemperature : LmuWindows(ReadoutItemKey.LmuWindows.TyreTemperature.Root)
        data object PitTiming : LmuWindows(ReadoutItemKey.LmuWindows.PitTiming.Root)
        data object RemainingVirtualEnergy :
            LmuWindows(ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root)
        data object TyreWear : LmuWindows(ReadoutItemKey.LmuWindows.TyreWear.Root)
        data object MyBestLap : LmuWindows(ReadoutItemKey.LmuWindows.MyBestLap.Root)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object MyBestLap : Gt7Ps5(ReadoutItemKey.Gt7Ps5.MyBestLap.Root)
        data object RemainingFuelLaps : Gt7Ps5(ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root)
        data object RemainingFuel : Gt7Ps5(ReadoutItemKey.Gt7Ps5.RemainingFuel.Root)
    }

    sealed class AceWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object RemainingFuel : AceWindows(ReadoutItemKey.AceWindows.RemainingFuel.Root)
    }

    companion object {
        fun fromId(simulator: Simulator, id: ReadoutItemKey): ReadoutListItemType? = when (simulator) {
            is Simulator.LmuWindows -> lmuWindowsFromId(id)
            is Simulator.Gt7Ps5 -> gt7Ps5FromId(id)
            is Simulator.AceWindows -> aceWindowsFromId(id)
        }

        private fun lmuWindowsFromId(id: ReadoutItemKey): LmuWindows? = when (id) {
            ReadoutItemKey.LmuWindows.VehicleApproach.Root -> LmuWindows.VehicleApproach
            ReadoutItemKey.LmuWindows.Flag.Root -> LmuWindows.Flag
            ReadoutItemKey.LmuWindows.VehicleDamage.Root -> LmuWindows.VehicleDamage
            ReadoutItemKey.LmuWindows.TyreTemperature.Root -> LmuWindows.TyreTemperature
            ReadoutItemKey.LmuWindows.PitTiming.Root -> LmuWindows.PitTiming
            ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> LmuWindows.RemainingVirtualEnergy
            ReadoutItemKey.LmuWindows.TyreWear.Root -> LmuWindows.TyreWear
            ReadoutItemKey.LmuWindows.MyBestLap.Root -> LmuWindows.MyBestLap
            else -> null
        }

        private fun gt7Ps5FromId(id: ReadoutItemKey): Gt7Ps5? = when (id) {
            ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> Gt7Ps5.MyBestLap
            ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> Gt7Ps5.RemainingFuelLaps
            ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> Gt7Ps5.RemainingFuel
            else -> null
        }

        private fun aceWindowsFromId(id: ReadoutItemKey): AceWindows? = when (id) {
            ReadoutItemKey.AceWindows.RemainingFuel.Root -> AceWindows.RemainingFuel
            else -> null
        }

        fun defaultOrder(simulator: Simulator): List<ReadoutItemKey> = when (simulator) {
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
                ReadoutItemKey.entries.filterIsInstance<ReadoutItemKey.AceWindows.TopLevel>()
            }
        }

        // listPane のトップレベル項目のみ並び順を持つ。
        // 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
        private fun lmuWindowsOrderIndex(key: ReadoutItemKey.LmuWindows.TopLevel): Int = when (key) {
            ReadoutItemKey.LmuWindows.Flag.Root -> 0
            ReadoutItemKey.LmuWindows.TyreTemperature.Root -> 1
            ReadoutItemKey.LmuWindows.VehicleApproach.Root -> 2
            ReadoutItemKey.LmuWindows.PitTiming.Root -> 3
            ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> 4
            ReadoutItemKey.LmuWindows.TyreWear.Root -> 5
            ReadoutItemKey.LmuWindows.VehicleDamage.Root -> 6
            ReadoutItemKey.LmuWindows.MyBestLap.Root -> 7
        }

        private fun gt7Ps5OrderIndex(key: ReadoutItemKey.Gt7Ps5.TopLevel): Int = when (key) {
            ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> 0
            ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> 1
            ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> 2
        }
    }
}
