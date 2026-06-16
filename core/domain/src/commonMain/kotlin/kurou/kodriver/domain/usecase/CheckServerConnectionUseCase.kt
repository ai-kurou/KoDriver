package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerConnectionRepository

class CheckServerConnectionUseCase(
    private val repository: ServerConnectionRepository,
) {
    suspend operator fun invoke(ip: String): Boolean = repository.isConnected(ip)
}
