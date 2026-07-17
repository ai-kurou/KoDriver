package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository

internal class JvmDynamicColorEnabledRepository : DynamicColorEnabledRepository {
    override fun dynamicColorEnabled(): Flow<Boolean> = flowOf(false)
    override suspend fun saveDynamicColorEnabled(enabled: Boolean) = Unit
}
