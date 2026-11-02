package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase

internal data class AceWindowsRemainingFuelReadoutTextUseCases(
    val fuel: ObserveAceWindowsRemainingFuelReadoutTextUseCase,
    val fuelLaps: ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase,
    val fuelLapsEmpty: ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase,
)

internal data class AceWindowsReadoutTextUseCases(
    val flag: ObserveAceWindowsFlagReadoutTextUseCase,
    val remainingFuel: AceWindowsRemainingFuelReadoutTextUseCases,
    val vehicleApproach: ObserveAceWindowsVehicleApproachReadoutTextUseCase,
    val tyreOverheat: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val myBestLap: ObserveAceWindowsMyBestLapReadoutTextUseCase,
)
