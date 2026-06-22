package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5Repository

class CheckGt7Ps5ConnectionUseCase(
    private val repository: Gt7Ps5Repository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
