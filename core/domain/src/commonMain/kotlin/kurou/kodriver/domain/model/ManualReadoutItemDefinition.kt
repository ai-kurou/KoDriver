package kurou.kodriver.domain.model

/** 手動読み上げ項目のカテゴリ。ピッカーとリストの分類・表示順の基準になる。 */
enum class ManualReadoutItemCategory {
    VIRTUAL_ENERGY,
    FUEL,
    TYRE,
    BRAKE,
    LAP,
}

/**
 * 手動読み上げ項目をシミュレーターとキーごとに定義する。
 *
 * [placeholder] は編集画面で挿入するプレースホルダー文字列、[findUnknownPlaceholders] は文言中の未知プレースホルダーを検出する関数、
 * [previewFormatter] はプレビュー用に文言へサンプル値を埋め込む関数。
 */
data class ManualReadoutItemDefinition(
    val key: String,
    val category: ManualReadoutItemCategory,
    val name: String,
    val textDefault: String,
    val placeholder: String,
    val findUnknownPlaceholders: (String) -> List<String>,
    val previewFormatter: (String) -> String,
)

fun manualReadoutItemDefinitionOf(
    key: ReadoutItemKey,
    category: ManualReadoutItemCategory,
    name: String,
    textDefault: String,
    placeholder: String,
    findUnknownPlaceholders: (String) -> List<String>,
    previewFormatter: (String) -> String,
): ManualReadoutItemDefinition =
    ManualReadoutItemDefinition(
        key.value,
        category,
        name,
        textDefault,
        placeholder,
        findUnknownPlaceholders,
        previewFormatter,
    )

fun manualReadoutItemDefinitionMapOf(
    vararg definitions: ManualReadoutItemDefinition,
): Map<String, ManualReadoutItemDefinition> = definitions.associateBy { it.key }
