package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository

internal class FakeExitConfirmationEnabledRepository(
    initial: Boolean = true,
) : ExitConfirmationEnabledRepository {
    private val flow = MutableStateFlow(initial)

    override fun exitConfirmationEnabled(): Flow<Boolean> = flow
    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) { flow.value = enabled }
}
