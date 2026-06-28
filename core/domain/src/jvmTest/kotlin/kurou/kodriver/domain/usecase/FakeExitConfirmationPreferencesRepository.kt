package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository

internal class FakeExitConfirmationPreferencesRepository(initial: Boolean = true) : ExitConfirmationPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun exitConfirmationEnabled(): Flow<Boolean> = flow
    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) { flow.value = enabled }
}
