package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5UdpPortPreferencesRepository

class ObserveGt7Ps5UdpPortUseCase(
    private val repository: Gt7Ps5UdpPortPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.port()
}
