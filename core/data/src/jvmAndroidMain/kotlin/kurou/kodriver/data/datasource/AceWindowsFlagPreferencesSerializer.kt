package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.AceWindowsFlagPreferences

internal val AceWindowsFlagPreferencesSerializer: Serializer<AceWindowsFlagPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = AceWindowsFlagPreferences(),
        kSerializer = AceWindowsFlagPreferences.serializer(),
        typeName = "AceWindowsFlagPreferences",
    )
