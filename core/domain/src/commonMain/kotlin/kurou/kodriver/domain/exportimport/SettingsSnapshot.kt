package kurou.kodriver.domain.exportimport

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class SettingsSnapshot(
    val schemaVersion: Int,
    val appVersion: String,
    val settings: Map<String, JsonElement>,
)
