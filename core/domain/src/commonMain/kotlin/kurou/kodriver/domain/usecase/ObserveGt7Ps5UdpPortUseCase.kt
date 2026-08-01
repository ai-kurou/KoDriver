package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository

class ObserveGt7Ps5UdpPortUseCase(
    private val repository: Gt7Ps5UdpPortPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.port()
}
