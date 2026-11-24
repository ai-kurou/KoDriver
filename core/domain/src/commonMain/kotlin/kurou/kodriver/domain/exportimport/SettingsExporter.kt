package kurou.kodriver.domain.exportimport

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

public class SettingsExporter(
    private val exportables: List<Exportable>,
    private val appVersion: String,
) {
    init {
        val duplicated =
            exportables
                .groupingBy { it.key }
                .eachCount()
                .filterValues { it > 1 }
                .keys
        require(duplicated.isEmpty()) { "Exportable のキーが重複しています: $duplicated" }
    }

    public suspend fun export(): String {
        val snapshot =
            SettingsSnapshot(
                schemaVersion = SCHEMA_VERSION,
                appVersion = appVersion,
                settings = exportables.associate { it.key to it.export() },
            )
        return json.encodeToString(SettingsSnapshot.serializer(), snapshot)
    }

    public suspend fun import(text: String): SettingsImportResult {
        val snapshot =
            try {
                json.decodeFromString(SettingsSnapshot.serializer(), text)
            } catch (_: SerializationException) {
                return SettingsImportResult.InvalidFormat
            } catch (_: IllegalArgumentException) {
                return SettingsImportResult.InvalidFormat
            }
        if (snapshot.schemaVersion > SCHEMA_VERSION) {
            return SettingsImportResult.UnsupportedSchemaVersion(snapshot.schemaVersion)
        }
        val imported = mutableListOf<String>()
        val failed = mutableListOf<String>()
        for (exportable in exportables) {
            val value = snapshot.settings[exportable.key] ?: continue
            try {
                exportable.import(value)
                imported += exportable.key
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                failed += exportable.key
            }
        }
        return SettingsImportResult.Completed(importedKeys = imported, failedKeys = failed)
    }

    public companion object {
        public const val SCHEMA_VERSION: Int = 1

        private val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
    }
}
