package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

internal class FakeServerIpPreferencesRepository(
    initial: String? = null,
) : ServerIpPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun serverIp(): Flow<String?> = flow
    override suspend fun saveServerIp(ip: String) { flow.value = ip }
}
