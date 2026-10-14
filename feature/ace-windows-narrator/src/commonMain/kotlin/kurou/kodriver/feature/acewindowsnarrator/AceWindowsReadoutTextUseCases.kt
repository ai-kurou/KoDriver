package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase

internal data class AceWindowsFlagReadoutTextUseCases(
    val checkered: ObserveAceWindowsCheckeredFlagReadoutTextUseCase,
    val white: ObserveAceWindowsWhiteFlagReadoutTextUseCase,
    val green: ObserveAceWindowsGreenFlagReadoutTextUseCase,
    val red: ObserveAceWindowsRedFlagReadoutTextUseCase,
    val blue: ObserveAceWindowsBlueFlagReadoutTextUseCase,
    val yellow: ObserveAceWindowsYellowFlagReadoutTextUseCase,
    val black: ObserveAceWindowsBlackFlagReadoutTextUseCase,
    val blackWhite: ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase,
    val orangeCircle: ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase,
    val redYellowStripes: ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase,
)

internal data class AceWindowsRemainingFuelReadoutTextUseCases(
    val fuel: ObserveAceWindowsRemainingFuelReadoutTextUseCase,
    val fuelLaps: ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase,
    val fuelLapsEmpty: ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase,
)

internal data class AceWindowsReadoutTextUseCases(
    val flag: AceWindowsFlagReadoutTextUseCases,
    val remainingFuel: AceWindowsRemainingFuelReadoutTextUseCases,
    val vehicleApproach: ObserveAceWindowsVehicleApproachReadoutTextUseCase,
    val tyreOverheat: ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase,
    val myBestLap: ObserveAceWindowsMyBestLapReadoutTextUseCase,
)
