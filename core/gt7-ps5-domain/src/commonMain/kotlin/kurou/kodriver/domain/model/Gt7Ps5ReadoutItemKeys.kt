package kurou.kodriver.domain.model

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5DefaultAutoReadoutOrder
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemDefinitions

val GT7_PS5_READOUT_ITEM_KEYS: List<ReadoutItemKey> =
    listOf(
        Gt7Ps5ReadoutItemKey.MyBestLap.Root,
        Gt7Ps5ReadoutItemKey.MyBestLap.DetailEnabled,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.DetailEnabled,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root,
        Gt7Ps5ReadoutItemKey.RemainingFuel.DetailEnabled,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root,
        Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning,
    )

val gt7Ps5ReadoutSpec: SimulatorReadoutSpec
    get() =
        SimulatorReadoutSpec(
            Simulator.Gt7Ps5,
            GT7_PS5_READOUT_ITEM_KEYS,
            GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT,
            GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT,
            GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT,
            gt7Ps5DefaultAutoReadoutOrder(),
            ManualReadoutSimulatorSpec(gt7Ps5ManualReadoutItemDefinitions, showSimHubGuideLink = false),
        )

fun registerGt7Ps5ReadoutItemKeys() = ReadoutItemKeyRegistry.register(gt7Ps5ReadoutSpec)
