package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository

internal class FakeGt7Ps5UdpPortPreferencesRepository(initial: Int = 33740) : Gt7Ps5UdpPortPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun port(): Flow<Int> = flow
    override suspend fun savePort(port: Int) { flow.update { port } }
}
