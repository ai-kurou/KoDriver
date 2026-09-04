package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.OverheatVoiceType
import kurou.kodriver.domain.repository.LmuWindowsOverheatPreferencesRepository

internal class LmuWindowsOverheatPreferencesRepositoryImpl(
    private val dataStore: DataStore<OverheatPreferences>,
) : LmuWindowsOverheatPreferencesRepository {
    override fun observeVoiceType(): Flow<OverheatVoiceType> =
        dataStore.observeProperty { OverheatVoiceType.fromId(it.voiceType) }

    override suspend fun saveVoiceType(type: OverheatVoiceType) {
        dataStore.saveProperty(type.id) { prefs, value -> prefs.copy(voiceType = value) }
    }
}
