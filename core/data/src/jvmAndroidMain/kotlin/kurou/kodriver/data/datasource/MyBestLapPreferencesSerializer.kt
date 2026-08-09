package kurou.kodriver.data.datasource

import androidx.datastore.core.Serializer
import kurou.kodriver.data.model.MyBestLapPreferences

internal val MyBestLapPreferencesSerializer: Serializer<MyBestLapPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MyBestLapPreferences(),
        kSerializer = MyBestLapPreferences.serializer(),
        typeName = "MyBestLapPreferences",
    )
