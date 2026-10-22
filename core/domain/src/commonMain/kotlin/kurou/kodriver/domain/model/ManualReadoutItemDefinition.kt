package kurou.kodriver.domain.model

/** 手動読み上げ項目をシミュレーターとキーごとに定義する。 */
data class ManualReadoutItemDefinition(
    val key: String,
    val name: String,
    val textDefault: String,
)

fun manualReadoutItemDefinitionOf(
    key: ReadoutItemKey,
    name: String,
    textDefault: String,
): ManualReadoutItemDefinition = ManualReadoutItemDefinition(key.value, name, textDefault)

fun manualReadoutItemDefinitionMapOf(
    vararg definitions: ManualReadoutItemDefinition,
): Map<String, ManualReadoutItemDefinition> = definitions.associateBy { it.key }
