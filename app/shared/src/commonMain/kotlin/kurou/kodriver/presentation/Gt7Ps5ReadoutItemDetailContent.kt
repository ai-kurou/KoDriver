package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.Gt7Ps5ReadoutMyBestLapDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.Gt7Ps5ReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.Gt7Ps5ReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.Gt7Ps5ReadoutTyreTemperatureDetailPane
import kurou.kodriver.feature.readoutlist.Gt7Ps5ReadoutListItemType

@Composable
internal fun Gt7Ps5ReadoutItemDetailContent(itemType: Gt7Ps5ReadoutListItemType) {
    when (itemType) {
        Gt7Ps5ReadoutListItemType.MyBestLap -> {
            Gt7Ps5ReadoutMyBestLapDetailPane()
        }

        Gt7Ps5ReadoutListItemType.RemainingFuelLaps -> {
            Gt7Ps5ReadoutRemainingFuelLapsDetailPane()
        }

        Gt7Ps5ReadoutListItemType.RemainingFuel -> {
            Gt7Ps5ReadoutRemainingFuelDetailPane()
        }

        Gt7Ps5ReadoutListItemType.TyreTemperature -> {
            Gt7Ps5ReadoutTyreTemperatureDetailPane()
        }
    }
}
