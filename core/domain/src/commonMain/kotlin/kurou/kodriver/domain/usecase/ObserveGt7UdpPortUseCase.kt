package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository

class ObserveGt7UdpPortUseCase(private val repository: Gt7UdpPortPreferencesRepository) {
    operator fun invoke(): Flow<Int> = repository.port()
}
