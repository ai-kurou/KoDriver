package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.Serializer

internal val MyBestLapPreferencesSerializer: Serializer<MyBestLapPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MyBestLapPreferences(),
        kSerializer = MyBestLapPreferences.serializer(),
    )
