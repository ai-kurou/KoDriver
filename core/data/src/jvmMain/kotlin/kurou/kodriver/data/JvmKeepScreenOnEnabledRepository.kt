package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

internal class JvmKeepScreenOnEnabledRepository : KeepScreenOnEnabledRepository {
    override fun keepScreenOn(): Flow<Boolean> = flowOf(false)
    override suspend fun saveKeepScreenOn(enabled: Boolean) = Unit
}
