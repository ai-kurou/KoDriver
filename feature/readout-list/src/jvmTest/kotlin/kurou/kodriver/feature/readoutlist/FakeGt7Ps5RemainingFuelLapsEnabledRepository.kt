package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository

internal class FakeGt7Ps5RemainingFuelLapsEnabledRepository(
    initialEnabled: Boolean = true,
) : Gt7Ps5RemainingFuelLapsEnabledRepository {
    private val enabled = MutableStateFlow(initialEnabled)

    override fun observeEnabled(): Flow<Boolean> = enabled

    override suspend fun saveEnabled(enabled: Boolean) {
        this.enabled.update { enabled }
    }
}
