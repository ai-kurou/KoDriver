package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuRepository

class DisconnectLmuUseCase(private val repository: LmuRepository) {
    suspend operator fun invoke() = repository.disconnect()
}
