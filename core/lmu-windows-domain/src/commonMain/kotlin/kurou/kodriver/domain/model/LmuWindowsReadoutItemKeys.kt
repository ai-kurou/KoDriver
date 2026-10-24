package kurou.kodriver.domain.model

import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsDefaultAutoReadoutOrder

val LMU_WINDOWS_READOUT_ITEM_KEYS: List<ReadoutItemKey> =
    listOf(
        LmuWindowsReadoutItemKey.VehicleApproach.Root,
        LmuWindowsReadoutItemKey.VehicleApproach.Sustained,
        LmuWindowsReadoutItemKey.VehicleApproach.StartReadout,
        LmuWindowsReadoutItemKey.Flag.Root,
        LmuWindowsReadoutItemKey.Flag.BlueFlag,
        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag,
        LmuWindowsReadoutItemKey.Flag.FullCourseYellow,
        LmuWindowsReadoutItemKey.Flag.RedFlag,
        LmuWindowsReadoutItemKey.VehicleDamage.Root,
        LmuWindowsReadoutItemKey.VehicleDamage.Overheat,
        LmuWindowsReadoutItemKey.VehicleDamage.PartDetached,
        LmuWindowsReadoutItemKey.VehicleDamage.TyreDetached,
        LmuWindowsReadoutItemKey.TyreTemperature.Root,
        LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
        LmuWindowsReadoutItemKey.TyreTemperature.LowWarning,
        LmuWindowsReadoutItemKey.PitTiming.Root,
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy,
        LmuWindowsReadoutItemKey.PitTiming.TyreWear,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.WarningReadout,
        LmuWindowsReadoutItemKey.RemainingFuel.Root,
        LmuWindowsReadoutItemKey.RemainingFuel.WarningReadout,
        LmuWindowsReadoutItemKey.TyreWear.Root,
        LmuWindowsReadoutItemKey.TyreWear.WarningReadout,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root,
        LmuWindowsReadoutItemKey.BrakeTemperature.WarningReadout,
        LmuWindowsReadoutItemKey.BrakeWear.Root,
        LmuWindowsReadoutItemKey.BrakeWear.WarningReadout,
        LmuWindowsReadoutItemKey.MyBestLap.Root,
        LmuWindowsReadoutItemKey.MyBestLap.DetailEnabled,
    )

val lmuWindowsReadoutSpec: SimulatorReadoutSpec
    get() =
        SimulatorReadoutSpec(
            Simulator.LmuWindows,
            LMU_WINDOWS_READOUT_ITEM_KEYS,
            LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
            LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
            LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT,
            lmuWindowsDefaultAutoReadoutOrder(),
        )

fun registerLmuWindowsReadoutItemKeys() = ReadoutItemKeyRegistry.register(lmuWindowsReadoutSpec)
