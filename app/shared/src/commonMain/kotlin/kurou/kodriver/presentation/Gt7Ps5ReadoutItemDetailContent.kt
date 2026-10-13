package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.autoreadoutlist.Gt7Ps5AutoReadoutListItemType
import kurou.kodriver.feature.gt7ps5readout.mybestlapdetail.Gt7Ps5ReadoutMyBestLapDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfueldetail.Gt7Ps5ReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.gt7ps5readout.remainingfuellapsdetail.Gt7Ps5ReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.gt7ps5readout.tyretemperaturedetail.Gt7Ps5ReadoutTyreTemperatureDetailPane

@Composable
internal fun Gt7Ps5ReadoutItemDetailContent(itemType: Gt7Ps5AutoReadoutListItemType) {
    when (itemType) {
        Gt7Ps5AutoReadoutListItemType.MyBestLap -> {
            Gt7Ps5ReadoutMyBestLapDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.RemainingFuelLaps -> {
            Gt7Ps5ReadoutRemainingFuelLapsDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.RemainingFuel -> {
            Gt7Ps5ReadoutRemainingFuelDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.TyreTemperature -> {
            Gt7Ps5ReadoutTyreTemperatureDetailPane()
        }
    }
}
