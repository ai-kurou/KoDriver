package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.exportimport.Exportable
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository

internal class AndroidAudioUsagePreferencesRepositoryImpl(
    private val dataStore: DataStore<AndroidAudioUsagePreferences>,
) : AndroidAudioUsagePreferencesRepository,
    Exportable by DataStoreExportable(
        key = "android_audio_usage",
        dataStore = dataStore,
        serializer = AndroidAudioUsagePreferences.serializer(),
    ) {
    override fun observeAndroidAudioUsage(): Flow<AndroidAudioUsage> =
        dataStore.observeProperty { AndroidAudioUsage.fromId(it.usage) }

    override suspend fun saveAndroidAudioUsage(androidAudioUsage: AndroidAudioUsage) {
        dataStore.saveProperty(androidAudioUsage.id) { prefs, value -> prefs.copy(usage = value) }
    }
}
