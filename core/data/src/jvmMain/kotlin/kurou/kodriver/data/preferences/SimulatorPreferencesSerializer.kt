package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val SimulatorPreferencesSerializer: Serializer<SimulatorPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SimulatorPreferences(),
        kSerializer = SimulatorPreferences.serializer(),
    )
