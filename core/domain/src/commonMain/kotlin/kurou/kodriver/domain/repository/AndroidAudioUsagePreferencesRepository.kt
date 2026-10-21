package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AndroidAudioUsage

interface AndroidAudioUsagePreferencesRepository {
    fun observeAndroidAudioUsage(): Flow<AndroidAudioUsage>

    suspend fun saveAndroidAudioUsage(androidAudioUsage: AndroidAudioUsage)
}
