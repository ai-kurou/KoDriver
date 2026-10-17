package kurou.kodriver.feature.gt7ps5narrator

import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5MyBestLapReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5RemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase

internal data class Gt7Ps5ReadoutTextUseCases(
    val remainingFuelLaps: ObserveGt7Ps5RemainingFuelLapsReadoutTextUseCase,
    val remainingFuelLapsEmpty: ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase,
    val remainingFuel: ObserveGt7Ps5RemainingFuelReadoutTextUseCase,
    val myBestLap: ObserveGt7Ps5MyBestLapReadoutTextUseCase,
    val tyreOverheat: ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase,
)
