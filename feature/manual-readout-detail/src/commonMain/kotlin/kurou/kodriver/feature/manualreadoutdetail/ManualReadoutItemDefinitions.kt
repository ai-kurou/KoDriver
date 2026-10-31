package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.ManualReadoutItemDefinition
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry

/** 登録済みの手動読み上げ対応シミュレーターごとの項目定義。 */
internal val manualReadoutItemDefinitions: Map<String, Map<String, ManualReadoutItemDefinition>>
    get() = ReadoutItemKeyRegistry.manualReadoutSpecs.associate { (id, spec) -> id to spec.itemDefinitions }

internal fun manualReadoutItemKeys(simulatorId: String): List<String> =
    manualReadoutItemDefinitions[simulatorId]?.keys.orEmpty().toList()

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
