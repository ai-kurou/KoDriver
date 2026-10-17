package kurou.kodriver.feature.acewindowsnarrator

import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlackWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsCheckeredFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsGreenFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRedYellowStripesFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsEmptyReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelLapsReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsRemainingFuelReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsVehicleApproachReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsWhiteFlagReadoutTextUseCase
import kurou.kodriver.domain.acewindows.usecase.ObserveAceWindowsYellowFlagReadoutTextUseCase

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
