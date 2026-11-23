package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.MicrophoneCutQuietSoundPreferencesRepository

internal class MicrophoneCutQuietSoundPreferencesRepositoryImpl(
    private val dataStore: DataStore<MicrophoneCutQuietSoundPreferences>,
) : MicrophoneCutQuietSoundPreferencesRepository {
    override fun observeMicrophoneCutQuietSoundLevel(): Flow<Int> = dataStore.observeProperty { it.level }

    override suspend fun saveMicrophoneCutQuietSoundLevel(level: Int) {
        dataStore.saveProperty(level) { prefs, value -> prefs.copy(level = value) }
    }
}
