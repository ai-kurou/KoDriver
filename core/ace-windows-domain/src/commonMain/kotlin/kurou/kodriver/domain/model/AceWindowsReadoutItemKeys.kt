package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.acewindows.model.aceWindowsDefaultAutoReadoutOrder
import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemDefinitions

val ACE_WINDOWS_READOUT_ITEM_KEYS: List<ReadoutItemKey> =
    listOf(
        AceWindowsReadoutItemKey.VehicleApproach.Root,
        AceWindowsReadoutItemKey.VehicleApproach.StartReadout,
        AceWindowsReadoutItemKey.Flag.Root,
        AceWindowsReadoutItemKey.Flag.WhiteFlag,
        AceWindowsReadoutItemKey.Flag.GreenFlag,
        AceWindowsReadoutItemKey.Flag.RedFlag,
        AceWindowsReadoutItemKey.Flag.BlueFlag,
        AceWindowsReadoutItemKey.Flag.YellowFlag,
        AceWindowsReadoutItemKey.Flag.BlackFlag,
        AceWindowsReadoutItemKey.Flag.BlackWhiteFlag,
        AceWindowsReadoutItemKey.Flag.CheckeredFlag,
        AceWindowsReadoutItemKey.Flag.OrangeCircleFlag,
        AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag,
        AceWindowsReadoutItemKey.RemainingFuel.Root,
        AceWindowsReadoutItemKey.RemainingFuel.DetailEnabled,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root,
        AceWindowsReadoutItemKey.RemainingFuelLaps.DetailEnabled,
        AceWindowsReadoutItemKey.TyreTemperature.Root,
        AceWindowsReadoutItemKey.TyreTemperature.OverheatWarning,
        AceWindowsReadoutItemKey.MyBestLap.Root,
        AceWindowsReadoutItemKey.MyBestLap.DetailEnabled,
    )

val aceWindowsReadoutSpec: SimulatorReadoutSpec
    get() =
        SimulatorReadoutSpec(
            Simulator.AceWindows,
            ACE_WINDOWS_READOUT_ITEM_KEYS,
            ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
            ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
            ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT,
            aceWindowsDefaultAutoReadoutOrder(),
            ManualReadoutSimulatorSpec(aceWindowsManualReadoutItemDefinitions),
        )

fun registerAceWindowsReadoutItemKeys() = ReadoutItemKeyRegistry.register(aceWindowsReadoutSpec)
