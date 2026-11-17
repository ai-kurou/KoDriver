package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AndroidAudioUsage

public interface AndroidAudioUsagePreferencesRepository {
    public fun observeAndroidAudioUsage(): Flow<AndroidAudioUsage>

    public suspend fun saveAndroidAudioUsage(androidAudioUsage: AndroidAudioUsage)
}
