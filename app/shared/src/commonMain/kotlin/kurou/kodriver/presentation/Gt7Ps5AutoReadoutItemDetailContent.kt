package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.autoreadoutlist.Gt7Ps5AutoReadoutListItemType
import kurou.kodriver.feature.gt7ps5autoreadout.mybestlapdetail.Gt7Ps5AutoReadoutMyBestLapDetailPane
import kurou.kodriver.feature.gt7ps5autoreadout.remainingfueldetail.Gt7Ps5AutoReadoutRemainingFuelDetailPane
import kurou.kodriver.feature.gt7ps5autoreadout.remainingfuellapsdetail.Gt7Ps5AutoReadoutRemainingFuelLapsDetailPane
import kurou.kodriver.feature.gt7ps5autoreadout.tyretemperaturedetail.Gt7Ps5AutoReadoutTyreTemperatureDetailPane

@Composable
internal fun Gt7Ps5AutoReadoutItemDetailContent(itemType: Gt7Ps5AutoReadoutListItemType) {
    when (itemType) {
        Gt7Ps5AutoReadoutListItemType.MyBestLap -> {
            Gt7Ps5AutoReadoutMyBestLapDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.RemainingFuelLaps -> {
            Gt7Ps5AutoReadoutRemainingFuelLapsDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.RemainingFuel -> {
            Gt7Ps5AutoReadoutRemainingFuelDetailPane()
        }

        Gt7Ps5AutoReadoutListItemType.TyreTemperature -> {
            Gt7Ps5AutoReadoutTyreTemperatureDetailPane()
        }
    }
}
