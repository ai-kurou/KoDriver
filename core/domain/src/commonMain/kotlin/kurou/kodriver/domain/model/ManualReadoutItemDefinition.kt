package kurou.kodriver.domain.model

/** 手動読み上げ項目のカテゴリ。ピッカーとリストの分類・表示順の基準になる。 */
enum class ManualReadoutItemCategory {
    VIRTUAL_ENERGY,
    FUEL,
    TYRE,
    BRAKE,
    LAP,
}

/** 手動読み上げ項目をシミュレーターとキーごとに定義する。 */
data class ManualReadoutItemDefinition(
    val key: String,
    val category: ManualReadoutItemCategory,
    val name: String,
    val textDefault: String,
)

fun manualReadoutItemDefinitionOf(
    key: ReadoutItemKey,
    category: ManualReadoutItemCategory,
    name: String,
    textDefault: String,
): ManualReadoutItemDefinition = ManualReadoutItemDefinition(key.value, category, name, textDefault)

fun manualReadoutItemDefinitionMapOf(
    vararg definitions: ManualReadoutItemDefinition,
): Map<String, ManualReadoutItemDefinition> = definitions.associateBy { it.key }
