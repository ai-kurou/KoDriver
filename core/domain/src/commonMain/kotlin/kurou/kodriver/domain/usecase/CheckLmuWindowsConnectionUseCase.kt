package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.LmuWindowsRepository

class CheckLmuWindowsConnectionUseCase(
    private val repository: LmuWindowsRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
