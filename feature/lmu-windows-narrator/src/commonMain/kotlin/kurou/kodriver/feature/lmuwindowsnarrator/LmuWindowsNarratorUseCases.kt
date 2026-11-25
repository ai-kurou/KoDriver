package kurou.kodriver.feature.lmuwindowsnarrator

import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsBrakeReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsFuelReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsManualReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsMyBestLapReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsPitTimingReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsRaceFlagReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsTyreReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVehicleReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.DetermineLmuWindowsVirtualEnergyReadoutUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearRemainingUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsClassCarGapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFlagEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFuelUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingFuelLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRaceFlagsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreCarcassTemperatureUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreDetachedUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearThresholdPercentageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedDurationUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleClassUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageEnabledStatesUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVirtualEnergyUseCase
import kurou.kodriver.domain.usecase.NotifyManualReadoutItemSpokenUseCase
import kurou.kodriver.domain.usecase.ObserveAutoReadoutEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutHotkeyPressesUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutItemRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutRequestsUseCase
import kurou.kodriver.domain.usecase.ObserveManualReadoutSetsUseCase
import kurou.kodriver.domain.usecase.ObserveQueueEnabledStatesUseCase
import kurou.kodriver.domain.usecase.ObserveResolvedAutoReadoutOrderUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal data class VehicleApproachUseCases(
    val observeVehicleApproach: ObserveLmuWindowsVehicleApproachUseCase,
    val observeLmuWindows: ObserveLmuWindowsUseCase,
    val observeSkipFirstLap: ObserveLmuWindowsVehicleApproachSkipFirstLapUseCase,
    val observeEnabledStates: ObserveLmuWindowsVehicleApproachEnabledStatesUseCase,
    val observeSustainedApproachDuration: ObserveLmuWindowsVehicleApproachSustainedDurationUseCase,
)

internal data class VehicleDamageUseCases(
    val observeVehicleDamage: ObserveLmuWindowsVehicleDamageUseCase,
    val observeVehicleDamageEnabledStates: ObserveLmuWindowsVehicleDamageEnabledStatesUseCase,
)

internal data class TyreDetachedUseCases(
    val observeTyreDetached: ObserveLmuWindowsTyreDetachedUseCase,
)

internal data class SimulatorUseCases(
    val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
)

internal data class ReadoutListUseCases(
    val observeAutoReadoutEnabledStates: ObserveAutoReadoutEnabledStatesUseCase,
    val observeAutoReadoutOrder: ObserveResolvedAutoReadoutOrderUseCase,
    val observeQueueEnabledStates: ObserveQueueEnabledStatesUseCase,
)

internal data class FlagUseCases(
    val observeRaceFlags: ObserveLmuWindowsRaceFlagsUseCase,
    val observeFlagEnabledStates: ObserveLmuWindowsFlagEnabledStatesUseCase,
)

internal data class TyreTemperatureUseCases(
    val observeTyreCarcassTemperature: ObserveLmuWindowsTyreCarcassTemperatureUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase,
    val observeVehicleClass: ObserveLmuWindowsVehicleClassUseCase,
    val observeTyreTemperatureEnabledStates: ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase,
    val observeLowWarningPhases: ObserveLmuWindowsTyreTemperatureLowWarningPhasesUseCase,
)

internal data class TyreWearUseCases(
    val observeTyreWear: ObserveLmuWindowsTyreWearUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsTyreWearThresholdPercentageUseCase,
)

internal data class RemainingVirtualEnergyUseCases(
    val observeRemainingVirtualEnergy: ObserveLmuWindowsVirtualEnergyUseCase,
    val observeManualReadoutHotkeyPresses: ObserveManualReadoutHotkeyPressesUseCase,
    val observeManualReadoutRequests: ObserveManualReadoutRequestsUseCase,
    val observeManualReadoutItemRequests: ObserveManualReadoutItemRequestsUseCase,
    val notifyManualReadoutItemSpoken: NotifyManualReadoutItemSpokenUseCase,
    val observeManualReadoutSets: ObserveManualReadoutSetsUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsRemainingVirtualEnergyThresholdPercentageUseCase,
)

internal data class RemainingFuelUseCases(
    val observeFuel: ObserveLmuWindowsFuelUseCase,
    val observeThresholdPercentage: ObserveLmuWindowsRemainingFuelThresholdPercentageUseCase,
)

internal data class BrakeTemperatureUseCases(
    val observeBrakeTemperature: ObserveLmuWindowsBrakeTemperatureUseCase,
    val observeVehicleClassHighThreshold: ObserveLmuWindowsVehicleClassBrakeTemperatureHighThresholdUseCase,
)

internal data class BrakeWearUseCases(
    val observeBrakeWear: ObserveLmuWindowsBrakeWearRemainingUseCase,
    val observeVehicleClassLowThreshold: ObserveLmuWindowsVehicleClassBrakeWearLowThresholdUseCase,
)

internal data class PitTimingUseCases(
    val observeVirtualEnergyLapsThreshold: ObserveLmuWindowsPitTimingVirtualEnergyLapsUseCase,
    val observeTyreWearLapsThreshold: ObserveLmuWindowsPitTimingTyreWearLapsUseCase,
    val observeFuelLapsThreshold: ObserveLmuWindowsPitTimingFuelLapsUseCase,
    val observeEnabledStates: ObserveLmuWindowsPitTimingEnabledStatesUseCase,
)

internal data class ClassCarGapUseCases(
    val observeClassCarGap: ObserveLmuWindowsClassCarGapUseCase,
)

internal data class NarratorUseCases(
    val myBestLapReadout: DetermineLmuWindowsMyBestLapReadoutUseCase,
    val vehicleReadout: DetermineLmuWindowsVehicleReadoutUseCase,
    val tyreReadout: DetermineLmuWindowsTyreReadoutUseCase,
    val brakeReadout: DetermineLmuWindowsBrakeReadoutUseCase,
    val virtualEnergyReadout: DetermineLmuWindowsVirtualEnergyReadoutUseCase,
    val fuelReadout: DetermineLmuWindowsFuelReadoutUseCase,
    val manualReadout: DetermineLmuWindowsManualReadoutUseCase,
    val pitTimingReadout: DetermineLmuWindowsPitTimingReadoutUseCase,
    val raceFlagReadout: DetermineLmuWindowsRaceFlagReadoutUseCase,
)
