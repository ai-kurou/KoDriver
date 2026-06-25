package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository

internal class FakeGt7UdpPortPreferencesRepository(initial: Int = 33740) : Gt7UdpPortPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun port(): Flow<Int> = flow
    override suspend fun savePort(port: Int) { flow.value = port }
}
