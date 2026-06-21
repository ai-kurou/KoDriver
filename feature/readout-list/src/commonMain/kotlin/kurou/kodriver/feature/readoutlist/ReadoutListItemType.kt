package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.ReadoutItemKey

sealed class ReadoutListItemType(val id: ReadoutItemKey) {
    sealed class LmuWindows(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object VehicleApproach : LmuWindows(ReadoutItemKey.VEHICLE_APPROACH)
        data object Flag : LmuWindows(ReadoutItemKey.FLAG)
        data object VehicleDamage : LmuWindows(ReadoutItemKey.VEHICLE_DAMAGE)
    }

    sealed class Gt7Ps5(id: ReadoutItemKey) : ReadoutListItemType(id) {
        data object BestLap : Gt7Ps5(ReadoutItemKey.BEST_LAP)
    }

    companion object {
        fun fromId(simulatorId: String, id: ReadoutItemKey): ReadoutListItemType? = when (simulatorId) {
            "lmu_windows" -> when (id) {
                ReadoutItemKey.VEHICLE_APPROACH -> LmuWindows.VehicleApproach
                ReadoutItemKey.FLAG -> LmuWindows.Flag
                ReadoutItemKey.VEHICLE_DAMAGE -> LmuWindows.VehicleDamage
                else -> null
            }
            "gt7_ps5" -> when (id) {
                ReadoutItemKey.BEST_LAP -> Gt7Ps5.BestLap
                else -> null
            }
            else -> null
        }
    }
}
