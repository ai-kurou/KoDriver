package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val OverheatPreferencesSerializer: Serializer<OverheatPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = OverheatPreferences(),
        kSerializer = OverheatPreferences.serializer(),
    )
