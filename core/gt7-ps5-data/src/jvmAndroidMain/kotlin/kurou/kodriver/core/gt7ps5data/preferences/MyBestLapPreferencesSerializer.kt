package kurou.kodriver.core.gt7ps5data.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
internal val MyBestLapPreferencesSerializer: Serializer<MyBestLapPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MyBestLapPreferences(),
        kSerializer = MyBestLapPreferences.serializer(),
    )
