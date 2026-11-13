package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SpeechRecognitionResumeDelayPreferencesRepository

internal class SpeechRecognitionResumeDelayPreferencesRepositoryImpl(
    private val dataStore: DataStore<SpeechRecognitionResumeDelayPreferences>,
) : SpeechRecognitionResumeDelayPreferencesRepository {
    override fun resumeDelayMs(): Flow<Int> = dataStore.observeProperty { it.resumeDelayMs }

    override suspend fun saveResumeDelayMs(delayMs: Int) {
        dataStore.saveProperty(delayMs) { prefs, value -> prefs.copy(resumeDelayMs = value) }
    }
}
