package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuRepository

class CheckLmuConnectionUseCase(
    private val repository: LmuRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
