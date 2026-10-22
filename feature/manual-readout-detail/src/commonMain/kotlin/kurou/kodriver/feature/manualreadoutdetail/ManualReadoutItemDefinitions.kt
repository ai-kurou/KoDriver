package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.acewindows.model.aceWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemDefinitions
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsManualReadoutItemDefinitions
import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.Simulator

internal val manualReadoutItemDefinitions: Map<String, Map<String, ManualReadoutItemDefinition>> =
    linkedMapOf(
        Simulator.LmuWindows.id to lmuWindowsManualReadoutItemDefinitions,
        Simulator.AceWindows.id to aceWindowsManualReadoutItemDefinitions,
        Simulator.Gt7Ps5.id to gt7Ps5ManualReadoutItemDefinitions,
    )

internal fun manualReadoutItemKeys(simulatorId: String): List<String> =
    manualReadoutItemDefinitions[simulatorId]?.keys?.toList().orEmpty()

internal fun manualReadoutItemDefinition(
    simulatorId: String,
    itemKey: String,
): ManualReadoutItemDefinition? = manualReadoutItemDefinitions[simulatorId]?.get(itemKey)

internal fun manualReadoutItemTextDefault(
    simulatorId: String,
    itemKey: String,
): String = manualReadoutItemDefinition(simulatorId, itemKey)?.textDefault.orEmpty()

internal fun manualReadoutItemName(
    simulatorId: String,
    itemKey: String,
): String = manualReadoutItemDefinition(simulatorId, itemKey)?.name.orEmpty()
