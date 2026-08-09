package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.SimulatorPreferences

internal val SimulatorPreferencesSerializer: Serializer<SimulatorPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = SimulatorPreferences(),
        kSerializer = SimulatorPreferences.serializer(),
    )
