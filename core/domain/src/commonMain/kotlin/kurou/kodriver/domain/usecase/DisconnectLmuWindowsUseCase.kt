package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsRepository

class DisconnectLmuWindowsUseCase(private val repository: LmuWindowsRepository) {
    suspend operator fun invoke() = repository.disconnect()
}
