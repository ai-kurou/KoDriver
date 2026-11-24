package kurou.kodriver.domain.exportimport

public sealed interface SettingsImportResult {
    /** 読み込めたキーと、検証・保存に失敗したキーの一覧。未知のキーは無視し、欠けているキーは変更しない。 */
    public data class Completed(
        val importedKeys: List<String>,
        val failedKeys: List<String>,
    ) : SettingsImportResult

    /** JSON として読めない、または必須項目が欠けている。 */
    public data object InvalidFormat : SettingsImportResult

    /** このアプリが対応する [SettingsExporter.SCHEMA_VERSION] より新しい形式。 */
    public data class UnsupportedSchemaVersion(
        val schemaVersion: Int,
    ) : SettingsImportResult
}
