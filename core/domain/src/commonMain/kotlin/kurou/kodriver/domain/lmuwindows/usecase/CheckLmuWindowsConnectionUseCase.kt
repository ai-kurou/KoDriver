package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository

class CheckLmuWindowsConnectionUseCase(
    private val repository: LmuWindowsRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
