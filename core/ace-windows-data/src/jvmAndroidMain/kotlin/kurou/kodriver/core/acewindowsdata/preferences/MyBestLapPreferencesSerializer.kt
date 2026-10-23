package kurou.kodriver.core.acewindowsdata.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
internal val MyBestLapPreferencesSerializer: Serializer<MyBestLapPreferences> =
    protoBufPreferencesSerializer(
        defaultValue = MyBestLapPreferences(),
        kSerializer = MyBestLapPreferences.serializer(),
    )
