package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository

internal class JvmExitConfirmationPreferencesRepository : ExitConfirmationPreferencesRepository {
    override fun exitConfirmationEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) = Unit
}
