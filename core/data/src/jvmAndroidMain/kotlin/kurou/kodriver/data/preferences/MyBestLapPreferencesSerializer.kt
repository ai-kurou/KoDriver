package kurou.kodriver.data.preferences

import androidx.datastore.core.Serializer

internal val MyBestLapPreferencesSerializer: Serializer<MyBestLapPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MyBestLapPreferences(),
        kSerializer = MyBestLapPreferences.serializer(),
    )
