package kurou.kodriver.feature.readoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperatureEnabledRepository

internal class FakeLmuWindowsTyreTemperatureEnabledRepository(
    initialEnabled: Boolean = false,
) : LmuWindowsTyreTemperatureEnabledRepository {
    private val enabled = MutableStateFlow(initialEnabled)

    override fun observeEnabled(): Flow<Boolean> = enabled

    override suspend fun saveEnabled(enabled: Boolean) {
        this.enabled.update { enabled }
    }
}
