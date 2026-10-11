package kurou.kodriver.feature.readoutlist

import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

sealed class Gt7Ps5ReadoutListItemType(
    id: ReadoutItemKey,
) : ReadoutListItemType(id) {
    data object MyBestLap : Gt7Ps5ReadoutListItemType(Gt7Ps5ReadoutItemKey.MyBestLap.Root)

    data object RemainingFuelLaps : Gt7Ps5ReadoutListItemType(Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root)

    data object RemainingFuel : Gt7Ps5ReadoutListItemType(Gt7Ps5ReadoutItemKey.RemainingFuel.Root)

    data object TyreTemperature : Gt7Ps5ReadoutListItemType(Gt7Ps5ReadoutItemKey.TyreTemperature.Root)

    companion object {
        fun fromId(id: ReadoutItemKey): Gt7Ps5ReadoutListItemType? =
            when (id) {
                Gt7Ps5ReadoutItemKey.MyBestLap.Root -> MyBestLap
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root -> RemainingFuelLaps
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root -> RemainingFuel
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root -> TyreTemperature
                else -> null
            }
    }
}
