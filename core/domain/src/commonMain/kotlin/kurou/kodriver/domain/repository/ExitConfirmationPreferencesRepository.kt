package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ExitConfirmationPreferencesRepository {
    fun exitConfirmationEnabled(): Flow<Boolean>
    suspend fun saveExitConfirmationEnabled(enabled: Boolean)
}
