package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.Gt7RemainingFuelLapsPreferencesRepository

internal class FakeGt7RemainingFuelLapsPreferencesRepository(
    initialEnabled: Boolean = true,
) : Gt7RemainingFuelLapsPreferencesRepository {
    private val enabled = MutableStateFlow(initialEnabled)

    override fun observeEnabled(): Flow<Boolean> = enabled

    override suspend fun saveEnabled(enabled: Boolean) {
        this.enabled.update { enabled }
    }
}
